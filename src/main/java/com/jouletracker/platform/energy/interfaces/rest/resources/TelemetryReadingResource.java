package com.jouletracker.platform.energy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Full electrical telemetry reading resource returned by TS-07 and TS-10 endpoints.
 */
@Schema(description = "Full electrical telemetry reading resource")
public record TelemetryReadingResource(
        @Schema(description = "Unique measurement ID", example = "1")
        Long id,

        @Schema(description = "Owner user ID", example = "1")
        Long userId,

        @Schema(description = "Source IoT sensor ID", example = "1")
        Long sensorId,

        @Schema(description = "Linked appliance device ID", example = "1")
        Long deviceId,

        @Schema(description = "Device display name", example = "Refrigeradora")
        String deviceName,

        @Schema(description = "Device category", example = "Electrodomésticos")
        String deviceCategory,

        @Schema(description = "Instantaneous active power in kW", example = "1.25")
        Double powerKw,

        @Schema(description = "Accumulated/delta energy in kWh", example = "0.42")
        Double energyKwh,

        @Schema(description = "Line voltage in Volts", example = "220.0")
        Double voltage,

        @Schema(description = "Electrical current in Amperes", example = "5.68")
        Double currentAmps,

        @Schema(description = "Calculated energy cost in PEN", example = "0.29")
        Double cost,

        @Schema(description = "Timestamp when the telemetry reading was recorded")
        LocalDateTime measuredAt
) {
}
