package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.commands.UpdateSensorCommand;
import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;
import com.jouletracker.platform.devices.interfaces.rest.resources.UpdateSensorResource;

/**
 * Assembler to translate {@link UpdateSensorResource} into {@link UpdateSensorCommand}.
 */
public class UpdateSensorCommandFromResourceAssembler {

    public static UpdateSensorCommand toCommandFromResource(Long sensorId, Long userId, UpdateSensorResource resource) {
        SensorNetworkStatus status = null;
        if (resource.status() != null && !resource.status().isBlank()) {
            status = SensorNetworkStatus.fromDisplayName(resource.status());
        }
        return new UpdateSensorCommand(
                sensorId,
                userId != null ? userId : 1L,
                resource.name(),
                resource.model(),
                status,
                resource.assignedDeviceId(),
                resource.applianceProfileId()
        );
    }
}
