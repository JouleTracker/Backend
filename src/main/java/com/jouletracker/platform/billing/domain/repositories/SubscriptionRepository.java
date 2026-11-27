package com.jouletracker.platform.billing.domain.repositories;

import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Domain repository port for {@link Subscription} aggregates.
 */
@NullMarked
public interface SubscriptionRepository {

    Optional<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Subscription save(Subscription subscription);
}
