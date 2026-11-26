package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST response representation of energy metrics for a physical business area (TS-17).
 */
@Schema(description = "Energy metrics for a single physical business area")
public record BusinessAreaMetricsResource(
        @Schema(description = "Business area ID", example = "1")
        Long areaId,

        @Schema(description = "Owner user ID", example = "1")
        Long userId,

        @Schema(description = "Business area name", example = "Cocina Industrial")
        String name,

        @Schema(description = "Physical floor or level", example = "Piso 1")
        String floor,

        @Schema(description = "Business area description", example = "Área de refrigeración y hornos")
        String description,

        @Schema(description = "Material icon identifier", example = "restaurant")
        String icon,

        @Schema(description = "Total devices linked to this area", example = "3")
        int deviceCount,

        @Schema(description = "Online devices count in this area", example = "2")
        int onlineDeviceCount,

        @Schema(description = "Current active power in kW", example = "2.45")
        double currentPowerKw,

        @Schema(description = "Accumulated energy in kWh for the selected period", example = "18.6")
        double todayKwh,

        @Schema(description = "Estimated cost in PEN for the selected period", example = "13.02")
        double todayCostSoles,

        @Schema(description = "Indicates whether the area has linked devices/data", example = "true")
        boolean hasDevices
) {
}
