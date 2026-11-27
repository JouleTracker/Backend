package com.jouletracker.platform.billing.application.internal;

import com.jouletracker.platform.billing.domain.model.aggregates.Subscription;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionStatus;
import com.jouletracker.platform.billing.domain.repositories.SubscriptionRepository;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import com.jouletracker.platform.billing.infrastructure.stripe.StripeProperties;
import com.jouletracker.platform.billing.infrastructure.stripe.StripeRestCheckoutGateway;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Billing & Subscriptions Use Cases Unit Tests (TS-18)")
class BillingUseCasesTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private StripeCheckoutGateway stripeCheckoutGateway;

    private ApplicationProperties applicationProperties;
    private CreateCheckoutSessionUseCaseImpl createCheckoutSessionUseCase;
    private ConfirmCheckoutSessionUseCaseImpl confirmCheckoutSessionUseCase;
    private HandleStripeWebhookUseCaseImpl handleStripeWebhookUseCase;

    @BeforeEach
    void setUp() {
        applicationProperties = new ApplicationProperties();
        applicationProperties.setFrontendUrl("https://jouletrackerfront.onrender.com,http://localhost:4200");

        createCheckoutSessionUseCase = new CreateCheckoutSessionUseCaseImpl(
                subscriptionRepository,
                stripeCheckoutGateway,
                applicationProperties
        );
        confirmCheckoutSessionUseCase = new ConfirmCheckoutSessionUseCaseImpl(
                subscriptionRepository,
                stripeCheckoutGateway
        );
        handleStripeWebhookUseCase = new HandleStripeWebhookUseCaseImpl(
                subscriptionRepository,
                stripeCheckoutGateway
        );
    }

    @Test
    @DisplayName("Should create Stripe Checkout session and record pending session ID")
    void shouldCreateCheckoutSessionSuccessfully() {
        when(subscriptionRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stripeCheckoutGateway.createCheckoutSession(
                eq(10L),
                eq("user@jouletracker.com"),
                eq(SubscriptionPlan.PLUS),
                eq("https://jouletrackerfront.onrender.com/inicio?checkout=success&session_id={CHECKOUT_SESSION_ID}"),
                eq("https://jouletrackerfront.onrender.com/inicio?checkout=cancelled")
        )).thenReturn(new StripeCheckoutGateway.CheckoutSessionCreated(
                "cs_test_123",
                "https://checkout.stripe.com/c/pay/cs_test_123",
                3900L,
                "pen"
        ));

        var result = createCheckoutSessionUseCase.handle(10L, "user@jouletracker.com", "plus");

        assertThat(result.isSuccess()).isTrue();
        var output = result.toOptional().orElseThrow();
        assertThat(output.sessionId()).isEqualTo("cs_test_123");
        assertThat(output.checkoutUrl()).isEqualTo("https://checkout.stripe.com/c/pay/cs_test_123");
        assertThat(output.plan()).isEqualTo("plus");
        verify(subscriptionRepository).save(argThat(sub -> "cs_test_123".equals(sub.getStripeCheckoutSessionId())));
    }

    @Test
    @DisplayName("Should reject unsupported plan names on checkout creation")
    void shouldRejectInvalidPlan() {
        var result = createCheckoutSessionUseCase.handle(10L, "user@jouletracker.com", "enterprise_ultra");

        assertThat(result.isFailure()).isTrue();
        verify(stripeCheckoutGateway, never()).createCheckoutSession(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should confirm paid Stripe Checkout session and activate purchased plan")
    void shouldConfirmPaidCheckoutSession() {
        when(stripeCheckoutGateway.retrieveCheckoutSession("cs_test_paid"))
                .thenReturn(new StripeCheckoutGateway.CheckoutSessionVerification(
                        "cs_test_paid", true, 10L, SubscriptionPlan.PRO, "cus_123"
                ));
        when(subscriptionRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = confirmCheckoutSessionUseCase.handle(10L, "cs_test_paid");

        assertThat(result.isSuccess()).isTrue();
        var sub = result.toOptional().orElseThrow();
        assertThat(sub.plan()).isEqualTo("pro");
        assertThat(sub.planCode()).isEqualTo("PRO");
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE.name());
    }

    @Test
    @DisplayName("Should forbid confirming a checkout session belonging to another user")
    void shouldForbidConfirmingAnotherUsersSession() {
        when(stripeCheckoutGateway.retrieveCheckoutSession("cs_test_other"))
                .thenReturn(new StripeCheckoutGateway.CheckoutSessionVerification(
                        "cs_test_other", true, 99L, SubscriptionPlan.PRO, "cus_99"
                ));

        var result = confirmCheckoutSessionUseCase.handle(10L, "cs_test_other");

        assertThat(result.isFailure()).isTrue();
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should verify Stripe HMAC-SHA256 webhook signature and fulfill checkout.session.completed")
    void shouldVerifyWebhookHmacAndActivateSubscription() throws Exception {
        String payload = """
                {
                  "id": "evt_1",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_webhook",
                      "customer": "cus_hook_1",
                      "client_reference_id": "10",
                      "metadata": {
                        "userId": "10",
                        "plan": "PLUS"
                      }
                    }
                  }
                }
                """;

        var props = new StripeProperties();
        props.setWebhookSecret("whsec_unit_test_secret");
        var gateway = new StripeRestCheckoutGateway(props);

        String timestamp = "1732640000";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("whsec_unit_test_secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String sig = HexFormat.of().formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
        String header = "t=" + timestamp + ",v1=" + sig;

        assertThat(gateway.verifyWebhookSignature(payload, header)).isTrue();
        assertThat(gateway.verifyWebhookSignature(payload, "t=" + timestamp + ",v1=invalidsig")).isFalse();

        when(stripeCheckoutGateway.verifyWebhookSignature(payload, header)).thenReturn(true);
        when(subscriptionRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        var webhookResult = handleStripeWebhookUseCase.handle(payload, header);

        assertThat(webhookResult.isSuccess()).isTrue();
        verify(subscriptionRepository).save(argThat(sub ->
                sub.getPlan() == SubscriptionPlan.PLUS && "cus_hook_1".equals(sub.getStripeCustomerId())
        ));
    }
}
