package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body to register a new IoT smart meter sensor")
public record RegisterSensorResource(
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 100)
        @Schema(description = "Sensor identifier name", example = "Sensor Cocina Principal")
        String name,

        @Schema(description = "Hardware model", example = "JoulePlug-v1")
        String model,

        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 50)
        @Schema(description = "Unique hardware serial number", example = "JT-SN-2026-001")
        String serialNumber,

        @Schema(description = "Assigned appliance identifier (optional)", example = "1")
        Long assignedDeviceId,

        @Schema(description = "Assigned appliance reference profile identifier (optional)", example = "1")
        Long applianceProfileId,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId
) {
}
