package com.jouletracker.platform.billing.application.internal;

import com.jouletracker.platform.billing.application.out.CheckoutSessionResult;
import com.jouletracker.platform.billing.application.usecases.CreateCheckoutSessionUseCase;
import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Locale;

/**
 * Orchestrates Stripe Checkout Session creation and records pending checkout references (TS-18).
 */
@Service
@NullMarked
public class CreateCheckoutSessionUseCaseImpl implements CreateCheckoutSessionUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCheckoutSessionUseCaseImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final StripeCheckoutGateway stripeCheckoutGateway;
    private final ApplicationProperties applicationProperties;

    public CreateCheckoutSessionUseCaseImpl(
            SubscriptionRepository subscriptionRepository,
            StripeCheckoutGateway stripeCheckoutGateway,
            ApplicationProperties applicationProperties) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeCheckoutGateway = stripeCheckoutGateway;
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Transactional
    public Result<CheckoutSessionResult, ApplicationError> handle(Long userId, String userEmail, String requestedPlan) {
        SubscriptionPlan plan;
        try {
            plan = SubscriptionPlan.fromString(requestedPlan);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("plan", ex.getMessage()));
        }

        String primaryFrontendUrl = resolvePrimaryFrontendUrl();
        String successUrl = primaryFrontendUrl + "/inicio?checkout=success&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = primaryFrontendUrl + "/inicio?checkout=cancelled";

        StripeCheckoutGateway.CheckoutSessionCreated created;
        try {
            created = stripeCheckoutGateway.createCheckoutSession(
                    userId,
                    userEmail,
                    plan,
                    successUrl,
                    cancelUrl
            );
        } catch (IllegalStateException ex) {
            LOGGER.error("Unable to create Stripe Checkout session for user {}: {}", userId, ex.getMessage());
            return Result.failure(ApplicationError.validationError("stripe", ex.getMessage()));
        }

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> Subscription.createDefault(userId));
        subscription.recordPendingCheckout(created.sessionId());
        subscriptionRepository.save(subscription);

        LOGGER.info("Created Stripe Checkout session {} for user {} targeting plan {}.",
                created.sessionId(), userId, plan.name());

        return Result.success(new CheckoutSessionResult(
                created.sessionId(),
                created.checkoutUrl(),
                plan.name().toLowerCase(Locale.ROOT),
                created.amountCents(),
                created.currency()
        ));
    }

    private String resolvePrimaryFrontendUrl() {
        return Arrays.stream(applicationProperties.getFrontendUrl().split(","))
                .map(String::trim)
                .map(origin -> origin.replaceAll("/+$", ""))
                .filter(origin -> !origin.isEmpty())
                .findFirst()
                .orElse("http://localhost:4200");
    }
}
