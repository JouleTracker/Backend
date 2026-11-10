package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.entities.SensorSummary;
import com.jouletracker.platform.devices.interfaces.rest.resources.SensorSummaryResource;

/**
 * Assembler to translate {@link SensorSummary} domain entity into {@link SensorSummaryResource}.
 */
public class SensorSummaryResourceFromEntityAssembler {

    public static SensorSummaryResource toResourceFromEntity(SensorSummary entity) {
        if (entity == null) {
            return null;
        }

        return new SensorSummaryResource(
                entity.id(),
                entity.totalSensors(),
                entity.activeSensors(),
                entity.monitoredDevicesCount(),
                entity.unassignedSensorsCount(),
                entity.totalMonitoredPowerKw(),
                entity.totalEnergyTodayKwh()
        );
    }
}
