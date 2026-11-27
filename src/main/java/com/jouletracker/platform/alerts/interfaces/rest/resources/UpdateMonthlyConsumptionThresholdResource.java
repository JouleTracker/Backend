
package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * US-26: Request resource for updating the global
 * monthly consumption threshold.
 */
public record UpdateMonthlyConsumptionThresholdResource(

        @NotNull(message = "Monthly consumption limit is required")
        @DecimalMin(
                value = "0.001",
                message = "Monthly consumption limit must be greater than zero"
        )
        @DecimalMax(
                value = "999999999.999",
                message = "Monthly consumption limit exceeds the maximum value"
        )
        @Digits(
                integer = 9,
                fraction = 3,
                message = "Monthly limit supports up to three decimal places"
        )
        @Schema(
                description = "Global monthly energy consumption limit in kWh",
                example = "150.000"
        )
        BigDecimal monthlyLimitKwh

) {
}
