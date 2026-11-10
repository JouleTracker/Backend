package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.commands.RegisterSensorCommand;
import com.jouletracker.platform.devices.interfaces.rest.resources.RegisterSensorResource;

/**
 * Assembler to translate {@link RegisterSensorResource} into {@link RegisterSensorCommand}.
 */
public class RegisterSensorCommandFromResourceAssembler {

    public static RegisterSensorCommand toCommandFromResource(RegisterSensorResource resource) {
        return toCommandFromResource(resource, resource.userId() != null ? resource.userId() : 1L);
    }

    public static RegisterSensorCommand toCommandFromResource(RegisterSensorResource resource, Long userId) {
        return new RegisterSensorCommand(
                userId,
                resource.name(),
                resource.model(),
                resource.serialNumber(),
                resource.assignedDeviceId(),
                resource.applianceProfileId()
        );
    }
}
