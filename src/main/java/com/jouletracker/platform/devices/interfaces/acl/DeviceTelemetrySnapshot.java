package com.jouletracker.platform.devices.interfaces.acl;

/**
 * Lightweight ACL snapshot of a monitored electrical device for cross-context metric aggregation.
 */
public record DeviceTelemetrySnapshot(
        Long deviceId,
        Long userId,
        String name,
        String location,
        String category,
        boolean online,
        Double currentPowerKw,
        Double powerLimitKw,
        Double todayKwh
) {
}
