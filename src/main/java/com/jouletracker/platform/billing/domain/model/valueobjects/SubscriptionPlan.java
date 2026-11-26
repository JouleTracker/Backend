package com.jouletracker.platform.billing.domain.model.valueobjects;

import java.util.Locale;

/**
 * Supported subscription tiers in JouleTracker Platform (TS-18 / US-46).
 */
public enum SubscriptionPlan {
    STARTER("Starter", 1900L),
    PLUS("Plus", 3900L),
    PRO("Pro", 7900L);

    private final String displayName;
    private final long monthlyPriceCents;

    SubscriptionPlan(String displayName, long monthlyPriceCents) {
        this.displayName = displayName;
        this.monthlyPriceCents = monthlyPriceCents;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getMonthlyPriceCents() {
        return monthlyPriceCents;
    }

    /**
     * Parses a plan string case-insensitively (e.g. {@code "plus"}, {@code "PLUS"}, {@code "Pro"}).
     *
     * @param value raw plan identifier
     * @return matching {@link SubscriptionPlan}
     * @throws IllegalArgumentException if blank or unsupported
     */
    public static SubscriptionPlan fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Subscription plan must not be blank");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (SubscriptionPlan plan : values()) {
            if (plan.name().equals(normalized)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unsupported subscription plan: " + value + ". Supported plans: STARTER, PLUS, PRO");
    }
}
