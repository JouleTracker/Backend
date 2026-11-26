package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request payload for initiating password recovery (TS-03).
 *
 * @param email the registered account email address
 */
@Schema(description = "Request payload to initiate password recovery")
public record ForgotPasswordRequest(
        @Schema(description = "Registered user email address", example = "john.doe@jouletracker.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email address is required")
        @Email(message = "Must be a valid email address")
        String email) {
}
