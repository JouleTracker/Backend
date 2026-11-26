package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response representation of an electrical device")
public record DeviceResource(
        @Schema(description = "Unique device identifier", example = "1")
        Long id,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @Schema(description = "Device descriptive name", example = "Refrigeradora Cocina")
        String name,

        @Schema(description = "Physical location or room", example = "Cocina")
        String location,

        @Schema(description = "Device category", example = "Electrodomésticos")
        String category,

        @Schema(description = "Operational state", example = "En línea")
        String status,

        @Schema(description = "Instantaneous power demand in kW", example = "0.15")
        Double currentPowerKw,

        @Schema(description = "Configured power threshold in kW", example = "0.8")
        Double powerLimitKw,

        @Schema(description = "Next scheduled maintenance date", example = "2026-12-15")
        String maintenanceDueDate,

        @Schema(description = "Accumulated energy consumption today in kWh", example = "1.8")
        Double todayKwh,

        @Schema(description = "Estimated cost for today's consumption in PEN", example = "1.26")
        Double todayCostSoles,

        @Schema(description = "Human-readable last activity indicator", example = "Hace 5 minutos")
        String lastActivity
) {
}
