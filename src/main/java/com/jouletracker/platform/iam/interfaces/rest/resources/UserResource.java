package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

/**
 * Public REST resource representing a user profile projection.
 */
@Schema(description = "User profile resource")
public record UserResource(
        @Schema(description = "Unique user identifier", example = "67417618-dd5e-11f1-a688-50ebf6e6183d")
        UUID id,

        @Schema(description = "Email address", example = "john.doe@jouletracker.com")
        String email,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Assigned security roles", example = "[\"ROLE_USER\"]")
        Set<String> roles,

        @Schema(description = "Whether the user email address is confirmed", example = "false")
        boolean emailVerified) {
}
