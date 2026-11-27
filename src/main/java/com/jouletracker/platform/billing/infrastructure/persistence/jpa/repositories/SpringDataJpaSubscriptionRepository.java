package com.jouletracker.platform.billing.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SubscriptionEntity}.
 */
@Repository
public interface SpringDataJpaSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findByUserId(Long userId);

    Optional<SubscriptionEntity> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);
}
