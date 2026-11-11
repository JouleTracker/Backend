package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.commands.CreateDeviceCommand;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.devices.interfaces.rest.resources.CreateDeviceResource;

/**
 * Assembler to translate {@link CreateDeviceResource} into {@link CreateDeviceCommand}.
 */
public class CreateDeviceCommandFromResourceAssembler {

    public static CreateDeviceCommand toCommandFromResource(CreateDeviceResource resource) {
        return toCommandFromResource(resource, resource.userId() != null ? resource.userId() : 1L);
    }

    public static CreateDeviceCommand toCommandFromResource(CreateDeviceResource resource, Long userId) {
        DeviceStatus status = null;
        if (resource.status() != null && !resource.status().isBlank()) {
            status = DeviceStatus.fromDisplayName(resource.status());
        }
        return new CreateDeviceCommand(
                userId,
                resource.name(),
                resource.location(),
                resource.category(),
                status,
                resource.powerLimitKw(),
                resource.maintenanceDueDate()
        );
    }
}
