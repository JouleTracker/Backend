package com.jouletracker.platform.devices.domain.model.commands;

import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;

/**
 * Command to create a new monitored electrical device.
 */
public record CreateDeviceCommand(
        Long userId,
        String name,
        String location,
        String category,
        DeviceStatus status,
        Double powerLimitKw,
        String maintenanceDueDate
) {
}
