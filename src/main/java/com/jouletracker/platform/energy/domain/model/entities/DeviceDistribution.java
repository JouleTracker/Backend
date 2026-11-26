package com.jouletracker.platform.energy.domain.model.entities;

/**
 * Domain read-model entity representing energy consumption breakdown by device or category.
 */
public record DeviceDistribution(
        Long deviceId,
        String deviceName,
        String category,
        Double totalKwh,
        Double totalCost,
        Double percentage
) {
}