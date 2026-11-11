package com.jouletracker.platform.devices.domain.model.commands;

/**
 * Command to remove a device and liberate assigned sensors.
 */
public record DeleteDeviceCommand(Long id, Long userId) {
}
