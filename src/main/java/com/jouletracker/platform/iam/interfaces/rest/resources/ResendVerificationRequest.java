package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request payload for resending an account verification email (US-04).
 *
 * @param email the unverified account email address
 */
@Schema(description = "Request payload to resend an email verification link")
public record ResendVerificationRequest(
        @Schema(description = "Unverified account email address", example = "john.doe@jouletracker.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email address is required")
        @Email(message = "Must be a valid email address")
        String email) {
}
