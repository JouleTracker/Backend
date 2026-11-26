package com.jouletracker.platform.businessenergy.domain.model.entities;

/**
 * Domain read-model representing real-time and period energy metrics for a physical business area.
 */
public record BusinessAreaMetrics(
        Long areaId,
        Long userId,
        String name,
        String floor,
        String description,
        String icon,
        int deviceCount,
        int onlineDeviceCount,
        double currentPowerKw,
        double todayKwh,
        double todayCostSoles,
        boolean hasDevices
) {
}
