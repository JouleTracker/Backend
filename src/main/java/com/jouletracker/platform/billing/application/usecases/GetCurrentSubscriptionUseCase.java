package com.jouletracker.platform.billing.application.usecases;

import com.jouletracker.platform.billing.application.out.SubscriptionResult;
import org.jspecify.annotations.NullMarked;

/**
 * Use case retrieving the current subscription plan for an authenticated user.
 */
@NullMarked
public interface GetCurrentSubscriptionUseCase {

    SubscriptionResult handle(Long userId);
}
