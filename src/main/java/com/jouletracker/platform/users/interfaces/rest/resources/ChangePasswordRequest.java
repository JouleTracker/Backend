package com.jouletracker.platform.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request payload for changing an authenticated user's password (US-25).
 *
 * @param currentPassword the user's current password for BCrypt verification
 * @param newPassword     the desired new password complying with platform complexity rules
 */
@Schema(description = "Request payload to update the authenticated user's password")
public record ChangePasswordRequest(
        @Schema(description = "Current account password", example = "CurrentPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password (8-64 chars, must include uppercase, lowercase, digit, and special character)", example = "NewSecurePass456!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 64, message = "New password must be between 8 and 64 characters")
        String newPassword) {
}
