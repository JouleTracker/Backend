package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Request body to update an existing monthly energy consumption goal (US-28).
 */
@Schema(description = "Request body to update a monthly energy consumption goal")
public record UpdateMonthlyGoalResource(
        @Positive(message = "Monthly target kWh must be greater than zero")
        @Schema(description = "Updated monthly consumption target in kWh", example = "300.0")
        Double targetKwh,

        @Min(2000)
        @Max(2100)
        @Schema(description = "Updated target year", example = "2026")
        Integer year,

        @Min(1)
        @Max(12)
        @Schema(description = "Updated target month 1-12", example = "11")
        Integer month
) {
}