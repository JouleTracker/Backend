package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request payload for completing a password reset with a single-use recovery token (TS-03).
 *
 * @param token       the single-use password reset token delivered via email
 * @param newPassword the new password complying with platform complexity rules
 */
@Schema(description = "Request payload to reset an account password using a single-use token")
public record ResetPasswordRequest(
        @Schema(description = "Single-use password recovery token received via email", example = "9xK2vR4pL8nQ1mZ7wB5cD3fG6hJ0kT2yU4vW8xY1zA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password reset token is required")
        String token,

        @Schema(description = "New password (8-64 chars, must include uppercase, lowercase, digit, and special character)", example = "NewSecurePass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
        String newPassword) {
}
