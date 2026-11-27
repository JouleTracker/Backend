package com.jouletracker.platform.billing.application.out;

import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Locale;

/**
 * Application projection representing a user's current subscription plan (TS-18 / US-46).
 */
@NullMarked
public record SubscriptionResult(
        Long userId,
        String plan,
        String planCode,
        String status,
        @Nullable Instant currentPeriodStart,
        @Nullable Instant currentPeriodEnd) {

    public static SubscriptionResult fromAggregate(Subscription subscription) {
        return new SubscriptionResult(
                subscription.getUserId(),
                subscription.getPlan().name().toLowerCase(Locale.ROOT),
                subscription.getPlan().name(),
                subscription.getStatus().name(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
