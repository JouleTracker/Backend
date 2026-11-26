package com.jouletracker.platform.devices.interfaces.acl;

/**
 * Lightweight ACL snapshot of a sensor's telemetry and configured consumption thresholds.
 */
public record SensorThresholdSnapshot(
        Long sensorId,
        Long userId,
        String sensorName,
        Long assignedDeviceId,
        String assignedDeviceName,
        Double currentPowerKw,
        Double todayKwh,
        Double recommendedDailyKwh,
        Double devicePowerLimitKw
) {
}
