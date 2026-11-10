package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.interfaces.rest.resources.DeviceResource;

/**
 * Assembler to translate {@link Device} domain entity into {@link DeviceResource}.
 */
public class DeviceResourceFromEntityAssembler {

    public static DeviceResource toResourceFromEntity(Device entity) {
        if (entity == null) {
            return null;
        }

        double todayKwh = entity.getTodayKwh() != null ? entity.getTodayKwh() : 0.0;
        double todayCost = Math.round(todayKwh * 0.70 * 100.0) / 100.0;

        return new DeviceResource(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getLocation(),
                entity.getCategory(),
                entity.getStatus() != null ? entity.getStatus().getDisplayName() : "Apagado",
                entity.getCurrentPowerKw(),
                entity.getPowerLimitKw(),
                entity.getMaintenanceDueDate(),
                todayKwh,
                todayCost,
                entity.getLastActivity()
        );
    }
}
