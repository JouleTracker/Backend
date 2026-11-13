package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public REST resource returned upon successful authentication.
 */
@Schema(description = "Authenticated user payload including Bearer JWT token")
public record AuthenticatedUserResource(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Token authentication scheme", example = "Bearer")
        String tokenType,

        @Schema(description = "Authenticated user profile")
        UserResource user) {
}
