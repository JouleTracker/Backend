package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Public REST resource representing a user profile projection.
 */
@Schema(description = "User profile resource")
public record UserResource(
        @Schema(description = "Unique user identifier", example = "1")
        Long id,

        @Schema(description = "Email address", example = "john.doe@jouletracker.com")
        String email,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Contact phone number", example = "+51 987654321")
        @Nullable String phone,

        @Schema(description = "Assigned security roles", example = "[\"ROLE_USER\"]")
        Set<String> roles,

        @Schema(description = "Whether the user email address is confirmed", example = "false")
        boolean emailVerified) {

    public UserResource(
            Long id,
            String email,
            String firstName,
            String lastName,
            Set<String> roles,
            boolean emailVerified) {
        this(id, email, firstName, lastName, null, roles, emailVerified);
    }
}
