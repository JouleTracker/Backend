package com.jouletracker.platform.energy.domain.model.events;

import java.time.LocalDateTime;

/**
 * Domain event published when a new energy telemetry measurement is registered.
 */
public record EnergyMeasurementRegisteredEvent(
        Long measurementId,
        Long userId,
        Long sensorId,
        Long deviceId,
        String deviceName,
        Double powerKw,
        Double energyKwh,
        LocalDateTime measuredAt
) {
}
