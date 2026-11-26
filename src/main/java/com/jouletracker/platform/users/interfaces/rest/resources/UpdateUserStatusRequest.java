package com.jouletracker.platform.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * REST request payload for administrative user moderation status updates (US-49).
 *
 * @param status target account status (ACTIVE, SUSPENDED, BANNED, INACTIVE)
 * @param reason optional administrative moderation reason
 */
@Schema(description = "Request payload for updating a user's account moderation status")
public record UpdateUserStatusRequest(
        @Schema(
                description = "Target account status",
                example = "BANNED",
                allowableValues = {"ACTIVE", "SUSPENDED", "BANNED", "INACTIVE"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Target status is required")
        String status,

        @Schema(description = "Optional reason explaining the moderation action", example = "Violation of platform terms of service")
        @Size(max = 255, message = "Moderation reason must not exceed 255 characters")
        @Nullable String reason) {
}
