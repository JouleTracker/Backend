package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request body to create a monthly energy consumption goal in kWh (US-28).
 */
@Schema(description = "Request body to create a monthly energy consumption goal")
public record CreateMonthlyGoalResource(
        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @NotNull(message = "Monthly target kWh is required")
        @Positive(message = "Monthly target kWh must be greater than zero")
        @Schema(description = "Monthly consumption target in kWh", example = "250.0")
        Double targetKwh,

        @Min(2000)
        @Max(2100)
        @Schema(description = "Target year (defaults to current year)", example = "2026")
        Integer year,

        @Min(1)
        @Max(12)
        @Schema(description = "Target month 1-12 (defaults to current month)", example = "11")
        Integer month
) {
}