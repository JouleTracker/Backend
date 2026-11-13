package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * REST response representation of aggregated MYPE dashboard energy metrics (TS-17).
 */
@Schema(description = "Aggregated business energy dashboard metrics across all areas")
public record BusinessDashboardMetricsResource(
        @Schema(description = "Owner user ID", example = "1")
        Long userId,

        @Schema(description = "Selected consumption period code", example = "daily")
        String period,

        @Schema(description = "Total number of registered areas", example = "3")
        int totalAreas,

        @Schema(description = "Combined active power across all areas in kW", example = "4.85")
        double totalPowerKw,

        @Schema(description = "Combined energy consumption in kWh", example = "34.2")
        double totalTodayKwh,

        @Schema(description = "Combined estimated cost in PEN", example = "23.94")
        double totalTodayCostSoles,

        @Schema(description = "Name of the area with highest energy consumption", example = "Cocina Industrial")
        String highestConsumptionAreaName,

        @Schema(description = "Per-area breakdown metrics")
        List<BusinessAreaMetricsResource> areas
) {
}
