package com.jouletracker.platform.iam.interfaces.rest;

import com.jouletracker.platform.iam.application.commands.ForgotPasswordCommand;
import com.jouletracker.platform.iam.application.commands.ResendVerificationCommand;
import com.jouletracker.platform.iam.application.commands.ResetPasswordCommand;
import com.jouletracker.platform.iam.application.commands.VerifyEmailCommand;
import com.jouletracker.platform.iam.application.usecases.ForgotPasswordUseCase;
import com.jouletracker.platform.iam.application.usecases.ResendVerificationUseCase;
import com.jouletracker.platform.iam.application.usecases.ResetPasswordUseCase;
import com.jouletracker.platform.iam.application.usecases.SignInUseCase;
import com.jouletracker.platform.iam.application.usecases.SignUpUseCase;
import com.jouletracker.platform.iam.application.usecases.VerifyEmailUseCase;
import com.jouletracker.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.jouletracker.platform.iam.interfaces.rest.resources.ForgotPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResendVerificationRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResetPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.UserResource;
import com.jouletracker.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.UserResourceAssembler;
import com.jouletracker.platform.shared.interfaces.rest.resources.MessageResource;
import com.jouletracker.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing IAM authentication, registration, account verification,
 * and password recovery endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, email confirmation, and password recovery")
@NullMarked
public class AuthenticationController {

    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthenticationController(
            SignUpUseCase signUpUseCase,
            SignInUseCase signInUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            ResendVerificationUseCase resendVerificationUseCase,
            ForgotPasswordUseCase forgotPasswordUseCase,
            ResetPasswordUseCase resetPasswordUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.signInUseCase = signInUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.resendVerificationUseCase = resendVerificationUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    /**
     * Registers a new user account and dispatches an email confirmation link (TS-01).
     *
     * @param request the sign-up request payload
     * @return 201 Created with created user profile, or RFC 9457 error
     */
    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register user", description = "Registers a new user and sends an email verification link via Brevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Email address already registered",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(request);
        var result = signUpUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceAssembler::toResourceFromUserResult,
                HttpStatus.CREATED
        );
    }

    /**
     * Authenticates a user with email and password, issuing a JWT access token (TS-02).
     *
     * @param request the sign-in request payload
     * @return 200 OK with Bearer token, or RFC 9457 error
     */
    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns a Bearer JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account email unverified, deactivated, suspended, or banned",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(request);
        var result = signInUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AuthenticatedUserResourceAssembler::toResourceFromAuthenticatedUserResult,
                HttpStatus.OK
        );
    }

    /**
     * Confirms a user's email address using a single-use verification token (US-04).
     *
     * @param token the single-use verification token string
     * @return 200 OK with updated user profile, or RFC 9457 error
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Confirms a user email address using a valid, single-use verification token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully verified",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or blank token",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Token not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Token expired or already used",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "Single-use verification token string", required = true)
            @RequestParam("token") String token) {
        var command = new VerifyEmailCommand(token);
        var result = verifyEmailUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                UserResourceAssembler::toResourceFromUserResult,
                HttpStatus.OK
        );
    }

    /**
     * Resends a fresh email verification link to an unverified user account via Brevo (US-04).
     *
     * @param request the resend-verification request payload
     * @return 200 OK with confirmation message, or RFC 9457 error
     */
    @PostMapping(value = "/resend-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Resend verification email",
            description = "Generates a new single-use verification token and dispatches a verification email via Brevo for an unverified account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification email resent successfully",
                    content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email format",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account is deactivated or restricted",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Account email is already verified",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        var command = new ResendVerificationCommand(request.email());
        var result = resendVerificationUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MessageResource::new,
                HttpStatus.OK
        );
    }

    /**
     * Initiates password recovery by generating an expiring single-use reset token and sending an email via Brevo (TS-03).
     *
     * @param request the forgot-password request payload
     * @return 200 OK with generic confirmation message, or 400 Bad Request for malformed email
     */
    @PostMapping(value = "/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Request password recovery",
            description = "Generates a secure, time-limited, single-use password recovery token and sends a reset link via Brevo using FRONTEND_BASE_URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password recovery request processed",
                    content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email address format",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        var command = new ForgotPasswordCommand(request.email());
        var result = forgotPasswordUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MessageResource::new,
                HttpStatus.OK
        );
    }

    /**
     * Completes password recovery by validating the single-use token and hashing the new password with BCrypt (TS-03).
     *
     * @param request the reset-password request payload
     * @return 200 OK with confirmation message, or RFC 9457 error
     */
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Reset password",
            description = "Validates the single-use password recovery token (existence, expiration, single-use) and updates the account password securely with BCrypt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password successfully reset",
                    content = @Content(schema = @Schema(implementation = MessageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token or weak password violating policy",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account is deactivated or restricted",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Password reset token not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Password reset token has expired or has already been used",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var command = new ResetPasswordCommand(request.token(), request.newPassword());
        var result = resetPasswordUseCase.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MessageResource::new,
                HttpStatus.OK
        );
    }
}
