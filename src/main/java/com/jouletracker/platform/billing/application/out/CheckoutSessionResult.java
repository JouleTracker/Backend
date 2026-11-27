package com.jouletracker.platform.billing.application.out;

import org.jspecify.annotations.NullMarked;

/**
 * Application output containing the created Stripe Checkout Session details.
 */
@NullMarked
public record CheckoutSessionResult(
        String sessionId,
        String checkoutUrl,
        String plan,
        long amountCents,
        String currency) {
}
