package com.jouletracker.platform.billing.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * REST resource representing the authenticated user's active subscription plan.
 */
@Schema(description = "User subscription resource")
public record SubscriptionResource(
        @Schema(description = "User ID", example = "1")
        Long userId,

        @Schema(description = "Normalized lowercase plan identifier", example = "plus")
        String plan,

        @Schema(description = "Canonical plan code", example = "PLUS")
        String planCode,

        @Schema(description = "Subscription status", example = "ACTIVE")
        String status,

        @Schema(description = "Current billing cycle start timestamp")
        @Nullable Instant currentPeriodStart,

        @Schema(description = "Current billing cycle end timestamp")
        @Nullable Instant currentPeriodEnd) {
}
