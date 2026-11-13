package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.interfaces.rest.resources.SensorResource;

/**
 * Assembler to translate {@link Sensor} domain entity into {@link SensorResource}.
 */
public class SensorResourceFromEntityAssembler {

    public static SensorResource toResourceFromEntity(Sensor entity) {
        if (entity == null) {
            return null;
        }

        double todayKwh = entity.getTodayKwh() != null ? entity.getTodayKwh() : 0.0;
        double todayCost = Math.round(todayKwh * 0.70 * 100.0) / 100.0;

        return new SensorResource(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getModel(),
                entity.getSerialNumber() != null ? entity.getSerialNumber().value() : null,
                entity.getStatus() != null ? entity.getStatus().getDisplayName() : "Fuera de línea",
                entity.getCurrentPowerKw(),
                todayKwh,
                todayCost,
                entity.getAssignedDeviceId(),
                entity.getAssignedDeviceName(),
                entity.getAssignedDeviceCategory(),
                entity.getApplianceProfileId(),
                entity.getApplianceProfileName(),
                entity.getRecommendedDailyKwh(),
                entity.getLastSync()
        );
    }
}
