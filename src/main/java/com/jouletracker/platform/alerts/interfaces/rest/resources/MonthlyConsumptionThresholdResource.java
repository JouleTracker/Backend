
package com.jouletracker.platform.alerts.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * US-26: Response resource representing the
 * user's monthly consumption threshold.
 */
public record MonthlyConsumptionThresholdResource(

        @Schema(example = "1")
        Long userId,

        @Schema(
                description = "Configured monthly consumption limit in kWh",
                example = "150.000",
                nullable = true
        )
        BigDecimal monthlyLimitKwh,

        @Schema(
                description = "Indicates whether the user has configured a limit",
                example = "true"
        )
        boolean configured

) {
}
