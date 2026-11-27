package com.jouletracker.platform.billing.infrastructure.stripe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

/**
 * Stripe REST API v1 client implementing {@link StripeCheckoutGateway} via Spring {@link RestClient}.
 */
@Component
@NullMarked
public class StripeRestCheckoutGateway implements StripeCheckoutGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeRestCheckoutGateway.class);

    private final StripeProperties properties;
    private final RestClient restClient;

    public StripeRestCheckoutGateway(StripeProperties properties) {
        this.properties = properties;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
        requestFactory.setReadTimeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public CheckoutSessionCreated createCheckoutSession(
            Long userId,
            String userEmail,
            SubscriptionPlan plan,
            String successUrl,
            String cancelUrl) {
        ensureSecretKeyConfigured();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        form.add("payment_method_types[0]", "card");
        form.add("line_items[0][price_data][currency]", properties.getCurrency());
        form.add("line_items[0][price_data][unit_amount]", String.valueOf(plan.getMonthlyPriceCents()));
        form.add("line_items[0][price_data][product_data][name]", "JouleTracker " + plan.getDisplayName() + " Plan");
        form.add("line_items[0][price_data][product_data][description]", "Monthly subscription to JouleTracker " + plan.getDisplayName());
        form.add("line_items[0][quantity]", "1");
        form.add("customer_email", userEmail);
        form.add("client_reference_id", String.valueOf(userId));
        form.add("metadata[userId]", String.valueOf(userId));
        form.add("metadata[plan]", plan.name());
        form.add("success_url", successUrl);
        form.add("cancel_url", cancelUrl);

        try {
            StripeSessionResponse response = restClient.post()
                    .uri("/v1/checkout/sessions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getSecretKey())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(StripeSessionResponse.class);

            if (response == null || response.id() == null || response.url() == null) {
                throw new IllegalStateException("Stripe returned an incomplete checkout session response");
            }

            return new CheckoutSessionCreated(
                    response.id(),
                    response.url(),
                    plan.getMonthlyPriceCents(),
                    properties.getCurrency()
            );
        } catch (RestClientResponseException ex) {
            LOGGER.error("Stripe checkout session creation failed with status {}: {}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Stripe Checkout session creation failed (HTTP " + ex.getStatusCode().value() + ")", ex);
        }
    }

    @Override
    public CheckoutSessionVerification retrieveCheckoutSession(String sessionId) {
        ensureSecretKeyConfigured();

        try {
            StripeSessionResponse response = restClient.get()
                    .uri("/v1/checkout/sessions/{sessionId}", sessionId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getSecretKey())
                    .retrieve()
                    .body(StripeSessionResponse.class);

            if (response == null || response.id() == null) {
                throw new IllegalStateException("Stripe returned an empty checkout session payload");
            }

            boolean paid = "paid".equalsIgnoreCase(response.paymentStatus());
            Long parsedUserId = parseUserId(response);
            SubscriptionPlan parsedPlan = parsePlan(response);

            return new CheckoutSessionVerification(
                    response.id(),
                    paid,
                    parsedUserId,
                    parsedPlan,
                    response.customer()
            );
        } catch (RestClientResponseException ex) {
            LOGGER.error("Stripe session retrieval failed with status {}: {}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Failed to verify Stripe Checkout session (HTTP " + ex.getStatusCode().value() + ")", ex);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, @Nullable String signatureHeader) {
        String webhookSecret = properties.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            LOGGER.warn("STRIPE_WEBHOOK_SECRET is blank; skipping HMAC webhook signature check.");
            return true;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }

        String timestamp = null;
        String v1Signature = null;
        for (String part : signatureHeader.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length == 2) {
                if ("t".equals(pair[0])) {
                    timestamp = pair[1];
                } else if ("v1".equals(pair[0])) {
                    v1Signature = pair[1];
                }
            }
        }
        if (timestamp == null || v1Signature == null) {
            return false;
        }

        try {
            String signedPayload = timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expectedSignature = HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    v1Signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            LOGGER.error("Error computing Stripe webhook HMAC signature", ex);
            return false;
        }
    }

    private void ensureSecretKeyConfigured() {
        if (properties.hasMissingSecretKey()) {
            throw new IllegalStateException("Stripe secret key (STRIPE_SECRET_KEY) is not configured");
        }
    }

    private static @Nullable Long parseUserId(StripeSessionResponse response) {
        String raw = null;
        if (response.metadata() != null && response.metadata().containsKey("userId")) {
            raw = response.metadata().get("userId");
        } else if (response.clientReferenceId() != null) {
            raw = response.clientReferenceId();
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static @Nullable SubscriptionPlan parsePlan(StripeSessionResponse response) {
        if (response.metadata() == null) {
            return null;
        }
        String rawPlan = response.metadata().get("plan");
        if (rawPlan == null || rawPlan.isBlank()) {
            return null;
        }
        try {
            return SubscriptionPlan.fromString(rawPlan);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StripeSessionResponse(
            String id,
            String url,
            @JsonProperty("payment_status") String paymentStatus,
            @JsonProperty("client_reference_id") String clientReferenceId,
            String customer,
            Map<String, String> metadata) {
    }
}
