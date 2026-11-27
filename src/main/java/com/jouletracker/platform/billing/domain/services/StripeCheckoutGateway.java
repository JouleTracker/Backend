package com.jouletracker.platform.billing.domain.services;

import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Outbound payment gateway port integrating Stripe Checkout and webhook verification (TS-18).
 */
@NullMarked
public interface StripeCheckoutGateway {

    record CheckoutSessionCreated(
            String sessionId,
            String checkoutUrl,
            long amountCents,
            String currency) {
    }

    record CheckoutSessionVerification(
            String sessionId,
            boolean paid,
            @Nullable Long userId,
            @Nullable SubscriptionPlan plan,
            @Nullable String customerId) {
    }

    CheckoutSessionCreated createCheckoutSession(
            Long userId,
            String userEmail,
            SubscriptionPlan plan,
            String successUrl,
            String cancelUrl);

    CheckoutSessionVerification retrieveCheckoutSession(String sessionId);

    boolean verifyWebhookSignature(String payload, @Nullable String signatureHeader);
}
