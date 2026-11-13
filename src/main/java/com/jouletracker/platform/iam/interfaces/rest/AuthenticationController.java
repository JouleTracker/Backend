package com.jouletracker.platform.iam.interfaces.rest;

import com.jouletracker.platform.iam.application.commands.VerifyEmailCommand;
import com.jouletracker.platform.iam.application.usecases.SignInUseCase;
import com.jouletracker.platform.iam.application.usecases.SignUpUseCase;
import com.jouletracker.platform.iam.application.usecases.VerifyEmailUseCase;
import com.jouletracker.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.UserResource;
import com.jouletracker.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.jouletracker.platform.iam.interfaces.rest.transform.UserResourceAssembler;
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
 * REST controller exposing user authentication, registration, and email verification endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, and email confirmation")
@NullMarked
public class AuthenticationController {

    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;

    public AuthenticationController(
            SignUpUseCase signUpUseCase,
            SignInUseCase signInUseCase,
            VerifyEmailUseCase verifyEmailUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.signInUseCase = signInUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
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
            @ApiResponse(responseCode = "403", description = "Email not confirmed",
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
     * Confirms a user's email address using a single-use verification token.
     *
     * @param token the single-use verification token string
     * @return 200 OK with updated user profile, or RFC 9457 error
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Confirms a user email address using a valid, single-use verification token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully verified",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
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
}
