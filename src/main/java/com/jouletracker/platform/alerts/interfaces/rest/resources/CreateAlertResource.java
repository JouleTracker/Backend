package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request payload for creating an administrative or broadcast consumption alert (US-50).
 */
@Schema(description = "Request payload to emit an administrative or broadcast consumption alert")
public record CreateAlertResource(
        @Schema(description = "Target user ID. Omit or set broadcast=true to send to all active users.", example = "2")
        Long userId,

        @Schema(description = "When true, broadcasts the alert to all active non-admin user accounts", example = "false")
        Boolean broadcast,

        @NotBlank(message = "Title is required")
        @Schema(description = "Alert title", example = "Peak demand advisory")
        String title,

        @Schema(description = "Detailed alert description", example = "Expected grid peak between 18:00 and 21:00.")
        String description,

        @Schema(description = "Fallback alert description alias", example = "Expected grid peak between 18:00 and 21:00.")
        String message,

        @Schema(description = "Alert severity code (info, warning, critical)", example = "warning")
        String severity,

        @Schema(description = "Target frontend route path", example = "/consumo")
        String actionUrl
) {
    /**
     * Resolves alert content from either {@code description} or fallback {@code message}.
     */
    public String resolvedDescription() {
        if (description != null && !description.isBlank()) {
            return description;
        }
        return message;
    }
}
