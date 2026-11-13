package com.jouletracker.platform.energy.domain.model.entities;

import java.time.LocalDateTime;

/**
 * Domain read-model entity representing a time-series point for energy consumption charts.
 */
public record EnergyReading(
        Long id,
        String label,
        LocalDateTime timestamp,
        Double kwh,
        Double powerKw,
        Double cost,
        Long deviceId,
        String deviceName
) {
}