package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body to partially configure an IoT sensor")
public record UpdateSensorResource(
        @Size(max = 100)
        @Schema(description = "Updated sensor name", example = "Sensor Refrigeradora")
        String name,

        @Schema(description = "Updated hardware model", example = "JoulePlug-Pro")
        String model,

        @Schema(description = "Sensor connectivity state", example = "En línea")
        String status,

        @Schema(description = "Linked device ID (null to unlink)", example = "1")
        Long assignedDeviceId,

        @Schema(description = "Linked reference profile ID (null to unlink)", example = "1")
        Long applianceProfileId
) {
}
