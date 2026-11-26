package com.jouletracker.platform.billing.application.internal;

import com.jouletracker.platform.billing.application.out.SubscriptionResult;
import com.jouletracker.platform.billing.application.usecases.GetCurrentSubscriptionUseCase;
import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retrieves the persisted subscription for the user, or returns the default active Starter tier.
 */
@Service
@NullMarked
public class GetCurrentSubscriptionUseCaseImpl implements GetCurrentSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;

    public GetCurrentSubscriptionUseCaseImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResult handle(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createDefault(userId));
        return SubscriptionResult.fromAggregate(subscription);
    }
}
