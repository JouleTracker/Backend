package com.jouletracker.platform.billing.application.usecases;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Use case processing asynchronous Stripe webhook events (`checkout.session.completed`).
 */
@NullMarked
public interface HandleStripeWebhookUseCase {

    Result<String, ApplicationError> handle(String payload, @Nullable String signatureHeader);
}
