package com.jouletracker.platform.billing.interfaces.rest.transform;

import com.jouletracker.platform.billing.application.out.CheckoutSessionResult;
import com.jouletracker.platform.billing.application.out.SubscriptionResult;
import com.jouletracker.platform.billing.interfaces.rest.resources.CheckoutSessionResource;
import com.jouletracker.platform.billing.interfaces.rest.resources.SubscriptionResource;
import org.jspecify.annotations.NullMarked;

/**
 * Assemblers converting billing application outputs into REST resources.
 */
@NullMarked
public final class SubscriptionResourceAssembler {

    private SubscriptionResourceAssembler() {
    }

    public static SubscriptionResource toResourceFromResult(SubscriptionResult result) {
        return new SubscriptionResource(
                result.userId(),
                result.plan(),
                result.planCode(),
                result.status(),
                result.currentPeriodStart(),
                result.currentPeriodEnd()
        );
    }

    public static CheckoutSessionResource toCheckoutResourceFromResult(CheckoutSessionResult result) {
        return new CheckoutSessionResource(
                result.sessionId(),
                result.checkoutUrl(),
                result.plan(),
                result.amountCents(),
                result.currency()
        );
    }
}
