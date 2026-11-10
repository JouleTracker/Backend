package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.commands.UpdateDeviceCommand;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.devices.interfaces.rest.resources.UpdateDeviceResource;

/**
 * Assembler to translate {@link UpdateDeviceResource} and device id into {@link UpdateDeviceCommand}.
 */
public class UpdateDeviceCommandFromResourceAssembler {

    public static UpdateDeviceCommand toCommandFromResource(Long deviceId, Long userId, UpdateDeviceResource resource) {
        DeviceStatus status = null;
        if (resource.status() != null && !resource.status().isBlank()) {
            status = DeviceStatus.fromDisplayName(resource.status());
        }
        return new UpdateDeviceCommand(
                deviceId,
                userId != null ? userId : 1L,
                resource.name(),
                resource.location(),
                resource.category(),
                status,
                resource.powerLimitKw(),
                resource.maintenanceDueDate()
        );
    }
}
