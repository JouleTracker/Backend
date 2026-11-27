package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST response representation of a monthly consumption goal and its progress (US-28).
 */
@Schema(description = "Monthly energy consumption goal resource with current progress")
public record MonthlyGoalResource(
        @Schema(description = "Unique monthly goal identifier", example = "1")
        Long id,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @Schema(description = "Monthly target consumption in kWh", example = "250.0")
        Double targetKwh,

        @Schema(description = "Target year", example = "2026")
        Integer year,

        @Schema(description = "Target month (1-12)", example = "11")
        Integer month,

        @Schema(description = "Accumulated energy consumed so far this month in kWh", example = "145.5")
        Double currentKwh,

        @Schema(description = "Consumption percentage relative to targetKwh", example = "58.2")
        Double progressPercentage,

        @Schema(description = "Remaining kWh before reaching the monthly limit", example = "104.5")
        Double remainingKwh,

        @Schema(description = "Whether monthly consumption has exceeded targetKwh", example = "false")
        boolean exceeded
) {
}