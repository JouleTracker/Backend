package com.jouletracker.platform.billing.application.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.billing.application.usecases.HandleStripeWebhookUseCase;
import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Verifies Stripe webhook signatures and activates subscriptions on {@code checkout.session.completed}.
 */
@Service
@NullMarked
public class HandleStripeWebhookUseCaseImpl implements HandleStripeWebhookUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleStripeWebhookUseCaseImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SubscriptionRepository subscriptionRepository;
    private final StripeCheckoutGateway stripeCheckoutGateway;

    public HandleStripeWebhookUseCaseImpl(
            SubscriptionRepository subscriptionRepository,
            StripeCheckoutGateway stripeCheckoutGateway) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeCheckoutGateway = stripeCheckoutGateway;
    }

    @Override
    @Transactional
    public Result<String, ApplicationError> handle(String payload, @Nullable String signatureHeader) {
        if (!stripeCheckoutGateway.verifyWebhookSignature(payload, signatureHeader)) {
            LOGGER.warn("Rejected Stripe webhook due to invalid signature.");
            return Result.failure(ApplicationError.validationError("Stripe-Signature", "Invalid webhook signature"));
        }

        JsonNode event;
        try {
            event = OBJECT_MAPPER.readTree(payload);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.validationError("payload", "Invalid Stripe webhook JSON payload"));
        }

        String eventType = event.path("type").asText("");
        if (!"checkout.session.completed".equals(eventType)) {
            return Result.success("Ignored Stripe event type: " + eventType);
        }

        JsonNode sessionObject = event.path("data").path("object");
        String sessionId = sessionObject.path("id").asText(null);
        String customerId = sessionObject.path("customer").asText(null);
        JsonNode metadata = sessionObject.path("metadata");
        String rawUserId = metadata.path("userId").asText(sessionObject.path("client_reference_id").asText(null));
        String rawPlan = metadata.path("plan").asText(null);

        if (rawUserId == null || rawUserId.isBlank() || rawPlan == null || rawPlan.isBlank()) {
            LOGGER.warn("Stripe webhook checkout.session.completed missing userId or plan metadata (sessionId={}).", sessionId);
            return Result.success("Checkout completed event missing user metadata");
        }

        Long userId;
        SubscriptionPlan plan;
        try {
            userId = Long.parseLong(rawUserId.trim());
            plan = SubscriptionPlan.fromString(rawPlan);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("metadata", "Invalid userId or plan in webhook metadata"));
        }

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createDefault(userId));
        subscription.activatePlan(plan, sessionId, customerId, Instant.now());
        subscriptionRepository.save(subscription);

        LOGGER.info("Stripe webhook activated plan {} for user {} (session {}).", plan.name(), userId, sessionId);
        return Result.success("Subscription activated for plan " + plan.name());
    }
}
