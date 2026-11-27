package com.jouletracker.platform.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST resource containing a hosted Stripe Checkout URL and session metadata.
 */
@Schema(description = "Stripe Checkout Session response resource")
public record CheckoutSessionResource(
        @Schema(description = "Stripe Checkout Session ID", example = "cs_test_a1B2c3D4")
        String sessionId,

        @Schema(description = "Hosted Stripe Checkout URL", example = "https://checkout.stripe.com/c/pay/cs_test_a1B2c3D4")
        String checkoutUrl,

        @Schema(description = "Selected subscription tier", example = "plus")
        String plan,

        @Schema(description = "Charge amount in minor currency units", example = "3900")
        long amountCents,

        @Schema(description = "ISO currency code", example = "pen")
        String currency) {
}
