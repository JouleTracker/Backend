package com.jouletracker.platform.devices.interfaces.rest.transform;

import com.jouletracker.platform.devices.domain.model.entities.DeviceSummary;
import com.jouletracker.platform.devices.interfaces.rest.resources.DeviceSummaryResource;

/**
 * Assembler to translate {@link DeviceSummary} domain entity into {@link DeviceSummaryResource}.
 */
public class DeviceSummaryResourceFromEntityAssembler {

    public static DeviceSummaryResource toResourceFromEntity(DeviceSummary entity) {
        if (entity == null) {
            return null;
        }

        return new DeviceSummaryResource(
                entity.id(),
                entity.connectedDevices(),
                entity.waitingDevices(),
                entity.totalPowerKw(),
                entity.powerDiffVsYesterday(),
                entity.powerDiffVsYesterdayLabel(),
                entity.costDiffVsMonth(),
                entity.costDiffVsMonthLabel(),
                entity.savedKwhMonthly(),
                entity.estimatedCostSoles()
        );
    }
}
