package com.jouletracker.platform.devices.domain.model.commands;

/**
 * Command to unlink/delete an IoT sensor.
 */
public record DeleteSensorCommand(Long id, Long userId) {
}
