
package com.jouletracker.platform.alerts.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US-38: Request resource for updating notification preferences.
 */
public record UpdateNotificationPreferencesResource(

        @NotNull(message = "Email preference is required")
        @Schema(example = "true")
        Boolean emailEnabled,

        @NotNull(message = "WhatsApp preference is required")
        @Schema(example = "false")
        Boolean whatsappEnabled

) {
}
