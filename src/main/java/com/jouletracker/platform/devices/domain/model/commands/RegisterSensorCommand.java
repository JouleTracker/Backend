package com.jouletracker.platform.devices.domain.model.commands;

/**
 * Command to register a new IoT smart meter sensor.
 */
public record RegisterSensorCommand(
        Long userId,
        String name,
        String model,
        String serialNumber,
        Long assignedDeviceId,
        Long applianceProfileId
) {
}
