package com.jouletracker.platform.devices.domain.model.commands;

import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;

/**
 * Command to update an IoT sensor configuration.
 */
public record UpdateSensorCommand(
        Long id,
        Long userId,
        String name,
        String model,
        SensorNetworkStatus status,
        Long assignedDeviceId,
        Long applianceProfileId
) {
}
