package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body to partially update an electrical device")
public record UpdateDeviceResource(
        @Size(max = 100)
        @Schema(description = "Updated device descriptive name", example = "Refrigeradora Samsung Inverter")
        String name,

        @Schema(description = "Updated room or location", example = "Cocina Principal")
        String location,

        @Schema(description = "Updated device category", example = "Línea Blanca")
        String category,

        @Schema(description = "Updated operational state", example = "En línea")
        String status,

        @Schema(description = "Updated power limit in kW", example = "1.0")
        Double powerLimitKw,

        @Schema(description = "Updated maintenance due date", example = "2027-01-10")
        String maintenanceDueDate
) {
}
