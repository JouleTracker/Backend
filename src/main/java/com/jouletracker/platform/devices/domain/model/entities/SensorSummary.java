package com.jouletracker.platform.devices.domain.model.entities;

/**
 * Summary record representing aggregated metrics for IoT sensors.
 */
public record SensorSummary(
        Long id,
        int totalSensors,
        int activeSensors,
        int monitoredDevicesCount,
        int unassignedSensorsCount,
        double totalMonitoredPowerKw,
        double totalEnergyTodayKwh
) {
}
