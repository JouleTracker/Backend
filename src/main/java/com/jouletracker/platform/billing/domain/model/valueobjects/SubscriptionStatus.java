package com.jouletracker.platform.billing.domain.model.valueobjects;

/**
 * Lifecycle status of a user subscription in JouleTracker.
 */
public enum SubscriptionStatus {
    ACTIVE,
    PENDING_PAYMENT,
    CANCELED
}
