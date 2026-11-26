package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response representation of an IoT smart meter sensor")
public record SensorResource(
        @Schema(description = "Unique sensor identifier", example = "1")
        Long id,

        @Schema(description = "Owner user identifier", example = "1")
        Long userId,

        @Schema(description = "Sensor identifier name", example = "Sensor Cocina")
        String name,

        @Schema(description = "Hardware model", example = "JoulePlug-v1")
        String model,

        @Schema(description = "Hardware serial number", example = "JT-SN-2026-001")
        String serialNumber,

        @Schema(description = "Sensor network status", example = "En línea")
        String status,

        @Schema(description = "Instantaneous power telemetry in kW", example = "0.15")
        Double currentPowerKw,

        @Schema(description = "Total accumulated energy today in kWh", example = "1.8")
        Double todayKwh,

        @Schema(description = "Calculated today cost in PEN (kWh * S/ 0.70)", example = "1.26")
        Double todayCostSoles,

        @Schema(description = "Assigned device identifier", example = "1")
        Long assignedDeviceId,

        @Schema(description = "Assigned device name", example = "Refrigeradora Cocina")
        String assignedDeviceName,

        @Schema(description = "Assigned device category", example = "Electrodomésticos")
        String assignedDeviceCategory,

        @Schema(description = "Assigned appliance profile ID", example = "1")
        Long applianceProfileId,

        @Schema(description = "Assigned appliance profile name", example = "Refrigeradora Inverter")
        String applianceProfileName,

        @Schema(description = "Benchmark recommended daily consumption in kWh", example = "1.8")
        Double recommendedDailyKwh,

        @Schema(description = "Timestamp of last received telemetry heartbeat", example = "Hace 2 minutos")
        String lastSync
) {
}
