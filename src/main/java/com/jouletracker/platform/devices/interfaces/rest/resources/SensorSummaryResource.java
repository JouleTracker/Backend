package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated KPI summary for user IoT sensors")
public record SensorSummaryResource(
        Long id,
        int totalSensors,
        int activeSensors,
        int monitoredDevicesCount,
        int unassignedSensorsCount,
        double totalMonitoredPowerKw,
        double totalEnergyTodayKwh
) {
}
