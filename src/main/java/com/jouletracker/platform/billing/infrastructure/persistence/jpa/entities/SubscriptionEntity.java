package com.jouletracker.platform.billing.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditablePersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity persisting user subscription plans and Stripe references in {@code subscriptions}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_subscriptions_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_subscriptions_user_id", columnList = "user_id"),
                @Index(name = "idx_subscriptions_checkout_session_id", columnList = "stripe_checkout_session_id")
        }
)
public class SubscriptionEntity extends AuditablePersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 30)
    private SubscriptionPlan plan = SubscriptionPlan.STARTER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "stripe_customer_id", length = 120)
    private String stripeCustomerId;

    @Column(name = "stripe_checkout_session_id", length = 160)
    private String stripeCheckoutSessionId;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    public SubscriptionEntity() {
    }
}
