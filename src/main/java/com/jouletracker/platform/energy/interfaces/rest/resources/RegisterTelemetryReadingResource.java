package com.jouletracker.platform.energy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

/**
 * Request payload for ingesting an IoT electrical telemetry reading (TS-07).
 */
@Schema(description = "IoT electrical telemetry reading payload")
public record RegisterTelemetryReadingResource(
        @Schema(description = "Owner user ID (defaults to 1 if omitted)", example = "1")
        Long userId,

        @Schema(description = "Source IoT sensor ID", example = "1")
        Long sensorId,

        @Schema(description = "Linked device ID", example = "1")
        Long deviceId,

        @Schema(description = "Device name", example = "Refrigeradora")
        String deviceName,

        @Schema(description = "Device category", example = "Electrodomésticos")
        String deviceCategory,

        @Schema(description = "Power in kW", example = "1.25")
        @PositiveOrZero(message = "powerKw must be zero or positive")
        Double powerKw,

        @Schema(description = "Energy in kWh", example = "0.42")
        @PositiveOrZero(message = "energyKwh must be zero or positive")
        Double energyKwh,

        @Schema(description = "Optional alias for energyKwh", example = "0.42")
        @PositiveOrZero(message = "kwh must be zero or positive")
        Double kwh,

        @Schema(description = "Voltage in Volts", example = "220.0")
        @PositiveOrZero(message = "voltage must be zero or positive")
        Double voltage,

        @Schema(description = "Current in Amperes", example = "5.68")
        @PositiveOrZero(message = "currentAmps must be zero or positive")
        Double currentAmps,

        @Schema(description = "Measurement timestamp (defaults to current time)")
        LocalDateTime measuredAt
) {
}
