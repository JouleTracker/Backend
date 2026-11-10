package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response representation of a catalog domestic appliance profile")
public record ApplianceProfileResource(
        @Schema(description = "Unique profile identifier", example = "1")
        Long id,

        @Schema(description = "Appliance type name", example = "Refrigeradora Inverter")
        String name,

        @Schema(description = "Benchmark recommended daily consumption in kWh", example = "1.8")
        Double recommendedDailyKwh,

        @Schema(description = "Typical operational power range in Watts", example = "150W - 250W")
        String typicalPowerWatts,

        @Schema(description = "Icon identifier for UI rendering", example = "kitchen")
        String icon
) {
}
