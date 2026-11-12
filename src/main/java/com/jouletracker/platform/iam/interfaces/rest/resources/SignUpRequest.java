package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user registration (POST /api/v1/authentication/sign-up).
 */
@Schema(description = "User registration payload")
public record SignUpRequest(
        @Schema(description = "User email address", example = "john.doe@jouletracker.com")
        @NotBlank(message = "{validation.not-blank}")
        @Email(message = "{validation.email}")
        String email,

        @Schema(description = "Plain text password (minimum 8 characters, containing uppercase, lowercase, digit, and special char)", example = "SecurePass123!")
        @NotBlank(message = "{validation.not-blank}")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
        )
        String password,

        @Schema(description = "User first name", example = "John")
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @Schema(description = "User last name", example = "Doe")
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName) {
}
