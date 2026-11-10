package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body to register a new electrical device")
public record CreateDeviceResource(
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 100)
        @Schema(description = "Device descriptive name", example = "Refrigeradora Cocina")
        String name,

        @Schema(description = "Physical room or location", example = "Cocina")
        String location,

        @Schema(description = "Device category", example = "Electrodomésticos")
        String category,

        @Schema(description = "Initial device operational state", example = "En línea")
        String status,

        @Schema(description = "Maximum recommended power limit in kW", example = "0.8")
        Double powerLimitKw,

        @Schema(description = "Next scheduled maintenance date", example = "2026-12-15")
        String maintenanceDueDate,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId
) {
}
