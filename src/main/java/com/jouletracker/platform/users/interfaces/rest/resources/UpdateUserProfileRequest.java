package com.jouletracker.platform.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Request payload for updating an authenticated user's profile (TS-12 / US-22).
 */
@Schema(description = "Request payload for updating user personal profile information")
public record UpdateUserProfileRequest(
        @Schema(description = "User's first name", example = "John")
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Schema(description = "Optional contact phone number", example = "+51 987654321")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        @Nullable String phone) {
}
