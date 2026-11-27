package com.jouletracker.platform.billing.application.usecases;

import com.jouletracker.platform.billing.application.out.CheckoutSessionResult;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Use case creating a Stripe Checkout Session for a subscription plan upgrade (TS-18).
 */
@NullMarked
public interface CreateCheckoutSessionUseCase {

    Result<CheckoutSessionResult, ApplicationError> handle(Long userId, String userEmail, String requestedPlan);
}
