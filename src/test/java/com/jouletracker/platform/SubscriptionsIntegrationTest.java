package com.jouletracker.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.billing.domain.model.valueobjects.SubscriptionPlan;
import com.jouletracker.platform.billing.domain.services.StripeCheckoutGateway;
import com.jouletracker.platform.billing.interfaces.rest.resources.ConfirmCheckoutSessionRequest;
import com.jouletracker.platform.billing.interfaces.rest.resources.CreateCheckoutSessionRequest;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.iam.domain.services.TokenService;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Billing & Subscriptions endpoints with Stripe Checkout gateway (TS-18 / US-46).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Subscriptions & Stripe Checkout Integration Tests")
class SubscriptionsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHashingService passwordHashingService;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private StripeCheckoutGateway stripeCheckoutGateway;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        reset(stripeCheckoutGateway);
    }

    private User createVerifiedUser(String prefix) {
        User user = User.create(
                EmailAddress.of(prefix + "-" + UUID.randomUUID() + "@jouletracker.com"),
                passwordHashingService.hash("SecurePass123!"),
                "Billing",
                "Tester",
                Set.of(Role.ROLE_USER)
        );
        user.confirmEmail();
        return userRepository.save(user);
    }

    @Test
    @DisplayName("[TS-18] Should require JWT for /me and /checkout-session, and activate plan via /confirm")
    void shouldCreateCheckoutAndConfirmPlanUpgrade() throws Exception {
        User user = createVerifiedUser("stripe-user");
        String jwt = tokenService.generateToken(user);

        // Unauthenticated -> 401
        mockMvc.perform(get("/api/v1/subscriptions/me"))
                .andExpect(status().isUnauthorized());

        // Default subscription before any upgrade -> STARTER
        mockMvc.perform(get("/api/v1/subscriptions/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("starter"))
                .andExpect(jsonPath("$.planCode").value("STARTER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        when(stripeCheckoutGateway.createCheckoutSession(
                eq(user.getId().value()),
                eq(user.getEmail().value()),
                eq(SubscriptionPlan.PRO),
                anyString(),
                anyString()
        )).thenReturn(new StripeCheckoutGateway.CheckoutSessionCreated(
                "cs_test_pro_upgrade",
                "https://checkout.stripe.com/c/pay/cs_test_pro_upgrade",
                7900L,
                "pen"
        ));

        mockMvc.perform(post("/api/v1/subscriptions/checkout-session")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCheckoutSessionRequest("PRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("cs_test_pro_upgrade"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.com/c/pay/cs_test_pro_upgrade"))
                .andExpect(jsonPath("$.plan").value("pro"));

        when(stripeCheckoutGateway.retrieveCheckoutSession("cs_test_pro_upgrade"))
                .thenReturn(new StripeCheckoutGateway.CheckoutSessionVerification(
                        "cs_test_pro_upgrade", true, user.getId().value(), SubscriptionPlan.PRO, "cus_pro_001"
                ));

        mockMvc.perform(post("/api/v1/subscriptions/confirm")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmCheckoutSessionRequest("cs_test_pro_upgrade"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("pro"))
                .andExpect(jsonPath("$.planCode").value("PRO"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Persisted in DB
        mockMvc.perform(get("/api/v1/subscriptions/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("pro"));
    }

    @Test
    @DisplayName("[TS-18] Should process valid Stripe webhook checkout.session.completed without JWT")
    void shouldProcessStripeWebhook() throws Exception {
        User user = createVerifiedUser("stripe-webhook");
        String jwt = tokenService.generateToken(user);

        String payload = """
                {
                  "id": "evt_test_webhook",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_hook_plus",
                      "customer": "cus_hook_plus",
                      "metadata": {
                        "userId": "%d",
                        "plan": "PLUS"
                      }
                    }
                  }
                }
                """.formatted(user.getId().value());

        when(stripeCheckoutGateway.verifyWebhookSignature(eq(payload), eq("t=123,v1=valid")))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/subscriptions/webhook")
                        .header("Stripe-Signature", "t=123,v1=valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/subscriptions/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("plus"))
                .andExpect(jsonPath("$.planCode").value("PLUS"));
    }
}
