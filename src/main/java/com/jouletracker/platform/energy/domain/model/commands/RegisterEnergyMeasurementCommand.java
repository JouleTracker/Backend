package com.jouletracker.platform.energy.domain.model.commands;

import java.time.LocalDateTime;

/**
 * Command to register a new electrical energy measurement from an IoT sensor or device.
 */
public record RegisterEnergyMeasurementCommand(
        Long userId,
        Long sensorId,
        Long deviceId,
        String deviceName,
        String deviceCategory,
        Double powerKw,
        Double energyKwh,
        Double voltage,
        Double currentAmps,
        LocalDateTime measuredAt
) {
}