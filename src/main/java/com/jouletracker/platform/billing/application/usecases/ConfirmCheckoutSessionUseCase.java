package com.jouletracker.platform.billing.application.usecases;

import com.jouletracker.platform.billing.application.out.SubscriptionResult;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Use case confirming a completed Stripe Checkout Session and activating the purchased plan.
 */
@NullMarked
public interface ConfirmCheckoutSessionUseCase {

    Result<SubscriptionResult, ApplicationError> handle(Long userId, String sessionId);
}
