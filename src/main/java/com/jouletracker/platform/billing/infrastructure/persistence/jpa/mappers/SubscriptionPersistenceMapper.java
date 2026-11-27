package com.jouletracker.platform.billing.infrastructure.persistence.jpa.mappers;

import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

/**
 * Mapper bridging between {@link Subscription} domain aggregates and {@link SubscriptionEntity}.
 */
@Component
@NullMarked
public class SubscriptionPersistenceMapper {

    public Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.getId(),
                entity.getUserId(),
                entity.getPlan(),
                entity.getStatus(),
                entity.getStripeCustomerId(),
                entity.getStripeCheckoutSessionId(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd()
        );
    }

    public SubscriptionEntity toEntity(Subscription domain) {
        var entity = new SubscriptionEntity();
        updateEntity(entity, domain);
        return entity;
    }

    public void updateEntity(SubscriptionEntity entity, Subscription domain) {
        entity.setUserId(domain.getUserId());
        entity.setPlan(domain.getPlan());
        entity.setStatus(domain.getStatus());
        entity.setStripeCustomerId(domain.getStripeCustomerId());
        entity.setStripeCheckoutSessionId(domain.getStripeCheckoutSessionId());
        entity.setCurrentPeriodStart(domain.getCurrentPeriodStart());
        entity.setCurrentPeriodEnd(domain.getCurrentPeriodEnd());
    }
}
