package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user sign-in authentication (POST /api/v1/authentication/sign-in).
 */
@Schema(description = "User authentication credentials")
public record SignInRequest(
        @Schema(description = "Registered email address", example = "john.doe@jouletracker.com")
        @NotBlank(message = "{validation.not-blank}")
        @Email(message = "{validation.email}")
        String email,

        @Schema(description = "User password", example = "SecurePass123!")
        @NotBlank(message = "{validation.not-blank}")
        String password) {
}
