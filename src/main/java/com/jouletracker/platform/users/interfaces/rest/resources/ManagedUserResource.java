package com.jouletracker.platform.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * REST resource representing a managed user account, including lifecycle status and deactivation metadata.
 */
@Schema(description = "Managed user account resource")
public record ManagedUserResource(
        @Schema(description = "Unique user identifier", example = "67417618-dd5e-11f1-a688-50ebf6e6183d")
        UUID id,

        @Schema(description = "User email address", example = "john.doe@jouletracker.com")
        String email,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Assigned security roles", example = "[\"ROLE_USER\"]")
        Set<String> roles,

        @Schema(description = "Current account lifecycle status", example = "ACTIVE")
        String status,

        @Schema(description = "Whether the user's email address is verified", example = "true")
        boolean emailVerified,

        @Schema(description = "UTC timestamp when the account was soft-deactivated, if applicable", example = "2026-11-25T23:30:00Z")
        @Nullable Instant deactivatedAt) {
}
