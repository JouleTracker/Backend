
package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US-38: Response resource representing the user's
 * persisted notification preferences.
 */
public record NotificationPreferencesResource(

        @Schema(example = "1")
        Long userId,

        @Schema(example = "true")
        boolean emailEnabled,

        @Schema(example = "false")
        boolean whatsappEnabled

) {
}
