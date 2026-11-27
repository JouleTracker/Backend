package com.jouletracker.platform.billing.application.internal;

import com.jouletracker.platform.billing.application.out.SubscriptionResult;
import com.jouletracker.platform.billing.application.usecases.ConfirmCheckoutSessionUseCase;
import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Verifies a Stripe Checkout Session directly against Stripe API and persists the active plan (TS-18).
 */
@Service
@NullMarked
public class ConfirmCheckoutSessionUseCaseImpl implements ConfirmCheckoutSessionUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmCheckoutSessionUseCaseImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final StripeCheckoutGateway stripeCheckoutGateway;

    public ConfirmCheckoutSessionUseCaseImpl(
            SubscriptionRepository subscriptionRepository,
            StripeCheckoutGateway stripeCheckoutGateway) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeCheckoutGateway = stripeCheckoutGateway;
    }

    @Override
    @Transactional
    public Result<SubscriptionResult, ApplicationError> handle(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Result.failure(ApplicationError.validationError("sessionId", "Checkout session ID is required"));
        }

        StripeCheckoutGateway.CheckoutSessionVerification verification;
        try {
            verification = stripeCheckoutGateway.retrieveCheckoutSession(sessionId.trim());
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.validationError("sessionId", ex.getMessage()));
        }

        if (!verification.paid()) {
            return Result.failure(ApplicationError.validationError(
                    "sessionId",
                    "Checkout session payment has not been completed yet"
            ));
        }

        if (verification.userId() != null && !verification.userId().equals(userId)) {
            LOGGER.warn("User {} attempted to confirm Stripe Checkout session {} belonging to user {}.",
                    userId, sessionId, verification.userId());
            return Result.failure(ApplicationError.forbidden("You are not authorized to confirm this checkout session"));
        }

        if (verification.plan() == null) {
            return Result.failure(ApplicationError.validationError(
                    "sessionId",
                    "Checkout session does not contain a valid subscription plan"
            ));
        }

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createDefault(userId));
        subscription.activatePlan(
                verification.plan(),
                verification.sessionId(),
                verification.customerId(),
                Instant.now()
        );
        Subscription saved = subscriptionRepository.save(subscription);

        LOGGER.info("Activated plan {} for user {} via Stripe Checkout session {}.",
                saved.getPlan().name(), userId, verification.sessionId());
        return Result.success(SubscriptionResult.fromAggregate(saved));
    }
}
