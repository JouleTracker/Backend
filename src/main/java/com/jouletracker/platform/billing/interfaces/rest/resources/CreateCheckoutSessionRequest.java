package com.jouletracker.platform.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body to initiate a Stripe Checkout Session for a subscription tier.
 */
@Schema(description = "Request payload to start a Stripe Checkout Session")
public record CreateCheckoutSessionRequest(
        @Schema(description = "Target subscription tier", example = "PLUS", allowableValues = {"STARTER", "PLUS", "PRO"})
        @NotBlank(message = "Subscription plan is required")
        String plan) {
}
