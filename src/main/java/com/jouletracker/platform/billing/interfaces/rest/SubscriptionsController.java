package com.jouletracker.platform.billing.interfaces.rest;

import com.jouletracker.platform.billing.application.usecases.ConfirmCheckoutSessionUseCase;
import com.jouletracker.platform.billing.application.usecases.CreateCheckoutSessionUseCase;
import com.jouletracker.platform.billing.application.usecases.GetCurrentSubscriptionUseCase;
import com.jouletracker.platform.billing.application.usecases.HandleStripeWebhookUseCase;
import com.jouletracker.platform.billing.interfaces.rest.resources.CheckoutSessionResource;
import com.jouletracker.platform.billing.interfaces.rest.resources.ConfirmCheckoutSessionRequest;
import com.jouletracker.platform.billing.interfaces.rest.resources.CreateCheckoutSessionRequest;
import com.jouletracker.platform.billing.interfaces.rest.resources.SubscriptionResource;
import com.jouletracker.platform.billing.interfaces.rest.transform.SubscriptionResourceAssembler;
import com.jouletracker.platform.iam.infrastructure.security.authentication.AuthenticatedUserDetails;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.interfaces.rest.resources.MessageResource;
import com.jouletracker.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Billing & Subscriptions bounded context exposing Stripe Checkout
 * session creation, confirmation, current subscription lookup, and webhook handling (TS-18 / US-46).
 */
@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Billing & Subscriptions", description = "Endpoints for Stripe Checkout subscription upgrades and plan status")
@NullMarked
public class SubscriptionsController {

    private final GetCurrentSubscriptionUseCase getCurrentSubscriptionUseCase;
    private final CreateCheckoutSessionUseCase createCheckoutSessionUseCase;
    private final ConfirmCheckoutSessionUseCase confirmCheckoutSessionUseCase;
    private final HandleStripeWebhookUseCase handleStripeWebhookUseCase;

    public SubscriptionsController(
            GetCurrentSubscriptionUseCase getCurrentSubscriptionUseCase,
            CreateCheckoutSessionUseCase createCheckoutSessionUseCase,
            ConfirmCheckoutSessionUseCase confirmCheckoutSessionUseCase,
            HandleStripeWebhookUseCase handleStripeWebhookUseCase) {
        this.getCurrentSubscriptionUseCase = getCurrentSubscriptionUseCase;
        this.createCheckoutSessionUseCase = createCheckoutSessionUseCase;
        this.confirmCheckoutSessionUseCase = confirmCheckoutSessionUseCase;
        this.handleStripeWebhookUseCase = handleStripeWebhookUseCase;
    }

    /**
     * Retrieves the authenticated user's active subscription plan.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get current subscription",
            description = "Retrieves the authenticated user's persisted subscription plan, defaulting to STARTER if no upgrade exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResource.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> getMySubscription(@AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        AuthenticatedUserDetails authenticated = resolveAuthenticatedUser(principal);
        if (authenticated == null || authenticated.getId() == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to query subscriptions")
            );
        }

        var result = getCurrentSubscriptionUseCase.handle(authenticated.getId());
        return ResponseEntity.ok(SubscriptionResourceAssembler.toResourceFromResult(result));
    }

    /**
     * Creates a hosted Stripe Checkout Session for the requested subscription tier (TS-18).
     */
    @PostMapping(value = "/checkout-session", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create Stripe Checkout Session",
            description = "Creates a Stripe Checkout Session in test/live mode for the requested plan (STARTER, PLUS, PRO)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stripe Checkout Session created",
                    content = @Content(schema = @Schema(implementation = CheckoutSessionResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid plan or missing Stripe configuration",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> createCheckoutSession(
            @Valid @RequestBody CreateCheckoutSessionRequest request,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        AuthenticatedUserDetails authenticated = resolveAuthenticatedUser(principal);
        if (authenticated == null || authenticated.getId() == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to initiate checkout")
            );
        }

        var result = createCheckoutSessionUseCase.handle(
                authenticated.getId(),
                authenticated.getUsername(),
                request.plan()
        );
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SubscriptionResourceAssembler::toCheckoutResourceFromResult,
                HttpStatus.OK
        );
    }

    /**
     * Verifies a completed Stripe Checkout Session and activates the user's purchased plan.
     */
    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Confirm Stripe Checkout Session",
            description = "Verifies payment status with Stripe API and activates the user's subscription tier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription activated",
                    content = @Content(schema = @Schema(implementation = SubscriptionResource.class))),
            @ApiResponse(responseCode = "400", description = "Session unpaid or invalid",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Session does not belong to authenticated user",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> confirmCheckoutSession(
            @Valid @RequestBody ConfirmCheckoutSessionRequest request,
            @AuthenticationPrincipal @Nullable AuthenticatedUserDetails principal) {
        AuthenticatedUserDetails authenticated = resolveAuthenticatedUser(principal);
        if (authenticated == null || authenticated.getId() == null) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                    ApplicationError.unauthorized("Full authentication is required to confirm checkout")
            );
        }

        var result = confirmCheckoutSessionUseCase.handle(authenticated.getId(), request.sessionId());
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                SubscriptionResourceAssembler::toResourceFromResult,
                HttpStatus.OK
        );
    }

    /**
     * Receives asynchronous Stripe webhook events (`checkout.session.completed`).
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Stripe webhook receiver",
            description = "Validates Stripe-Signature HMAC header and fulfills checkout.session.completed events."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed",
                    content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid webhook signature or payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) @Nullable String stripeSignature) {
        var result = handleStripeWebhookUseCase.handle(payload, stripeSignature);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MessageResource::new,
                HttpStatus.OK
        );
    }

    private @Nullable AuthenticatedUserDetails resolveAuthenticatedUser(
            @Nullable AuthenticatedUserDetails principal) {
        if (principal != null) {
            return principal;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserDetails details) {
            return details;
        }
        return null;
    }
}
