package com.jouletracker.platform.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body to verify and confirm a completed Stripe Checkout Session.
 */
@Schema(description = "Request payload to confirm a Stripe Checkout Session")
public record ConfirmCheckoutSessionRequest(
        @Schema(description = "Stripe Checkout Session identifier", example = "cs_test_a1B2c3D4")
        @NotBlank(message = "Checkout session ID is required")
        String sessionId) {
}
