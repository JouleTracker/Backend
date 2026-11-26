package com.jouletracker.platform.devices.domain.model.commands;

import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;

/**
 * Command to update an existing electrical device.
 */
public record UpdateDeviceCommand(
        Long id,
        Long userId,
        String name,
        String location,
        String category,
        DeviceStatus status,
        Double powerLimitKw,
        String maintenanceDueDate
) {
}
