package com.jouletracker.platform.billing.domain.model.aggregates;

import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Aggregate root representing a user's active or pending subscription plan (TS-18).
 */
@NullMarked
public class Subscription {

    private @Nullable Long id;
    private final Long userId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private @Nullable String stripeCustomerId;
    private @Nullable String stripeCheckoutSessionId;
    private @Nullable Instant currentPeriodStart;
    private @Nullable Instant currentPeriodEnd;

    public Subscription(
            @Nullable Long id,
            Long userId,
            SubscriptionPlan plan,
            SubscriptionStatus status,
            @Nullable String stripeCustomerId,
            @Nullable String stripeCheckoutSessionId,
            @Nullable Instant currentPeriodStart,
            @Nullable Instant currentPeriodEnd) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "User ID must not be null");
        this.plan = Objects.requireNonNull(plan, "Subscription plan must not be null");
        this.status = Objects.requireNonNull(status, "Subscription status must not be null");
        this.stripeCustomerId = stripeCustomerId;
        this.stripeCheckoutSessionId = stripeCheckoutSessionId;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
    }

    /**
     * Creates a default active Starter subscription for a user without a persisted billing record yet.
     *
     * @param userId the user identifier
     * @return default {@link Subscription}
     */
    public static Subscription createDefault(Long userId) {
        Instant now = Instant.now();
        return new Subscription(
                null,
                userId,
                SubscriptionPlan.STARTER,
                SubscriptionStatus.ACTIVE,
                null,
                null,
                now,
                now.plus(30, ChronoUnit.DAYS)
        );
    }

    /**
     * Records the latest Stripe Checkout session ID while keeping existing plan entitlements until paid.
     *
     * @param checkoutSessionId Stripe Checkout Session ID
     */
    public void recordPendingCheckout(String checkoutSessionId) {
        this.stripeCheckoutSessionId = Objects.requireNonNull(checkoutSessionId, "Checkout session ID must not be null");
    }

    /**
     * Activates the purchased subscription plan for a 30-day billing cycle.
     *
     * @param newPlan           the purchased plan tier
     * @param checkoutSessionId the Stripe Checkout Session ID
     * @param customerId        optional Stripe Customer ID
     * @param activatedAt       activation timestamp
     */
    public void activatePlan(
            SubscriptionPlan newPlan,
            @Nullable String checkoutSessionId,
            @Nullable String customerId,
            Instant activatedAt) {
        this.plan = Objects.requireNonNull(newPlan, "New plan must not be null");
        this.status = SubscriptionStatus.ACTIVE;
        if (checkoutSessionId != null && !checkoutSessionId.isBlank()) {
            this.stripeCheckoutSessionId = checkoutSessionId;
        }
        if (customerId != null && !customerId.isBlank()) {
            this.stripeCustomerId = customerId;
        }
        Objects.requireNonNull(activatedAt, "Activation timestamp must not be null");
        this.currentPeriodStart = activatedAt;
        this.currentPeriodEnd = activatedAt.plus(30, ChronoUnit.DAYS);
    }

    public @Nullable Long getId() {
        return id;
    }

    public void assignId(Long id) {
        this.id = Objects.requireNonNull(id, "Subscription ID must not be null");
    }

    public Long getUserId() {
        return userId;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public @Nullable String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public @Nullable String getStripeCheckoutSessionId() {
        return stripeCheckoutSessionId;
    }

    public @Nullable Instant getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public @Nullable Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }
}
