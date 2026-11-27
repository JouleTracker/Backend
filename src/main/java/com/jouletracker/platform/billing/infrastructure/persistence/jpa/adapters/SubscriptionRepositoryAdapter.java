package com.jouletracker.platform.billing.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import com.jouletracker.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionEntity;
import com.jouletracker.platform.billing.infrastructure.persistence.jpa.mappers.SubscriptionPersistenceMapper;
import com.jouletracker.platform.billing.infrastructure.persistence.jpa.repositories.SpringDataJpaSubscriptionRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * JPA repository adapter implementing {@link SubscriptionRepository}.
 */
@Repository
@NullMarked
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SpringDataJpaSubscriptionRepository jpaRepository;
    private final SubscriptionPersistenceMapper mapper;

    public SubscriptionRepositoryAdapter(
            SpringDataJpaSubscriptionRepository jpaRepository,
            SubscriptionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findByStripeCheckoutSessionId(String stripeCheckoutSessionId) {
        return jpaRepository.findByStripeCheckoutSessionId(stripeCheckoutSessionId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = jpaRepository.findByUserId(subscription.getUserId())
                .map(existing -> {
                    mapper.updateEntity(existing, subscription);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(subscription));

        SubscriptionEntity saved = jpaRepository.save(entity);
        Subscription mapped = mapper.toDomain(saved);
        if (subscription.getId() == null && mapped.getId() != null) {
            subscription.assignId(mapped.getId());
        }
        return mapped;
    }
}
