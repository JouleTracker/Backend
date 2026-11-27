
package com.jouletracker.platform.alerts.interfaces.rest.transform;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.interfaces.rest.resources.MonthlyConsumptionThresholdResource;

/**
 * US-26: Converts monthly consumption thresholds
 * into REST response resources.
 */
public final class MonthlyConsumptionThresholdResourceFromEntityAssembler {

    private MonthlyConsumptionThresholdResourceFromEntityAssembler() {
    }

    public static MonthlyConsumptionThresholdResource toResourceFromEntity(
            MonthlyConsumptionThreshold entity) {

        return new MonthlyConsumptionThresholdResource(
                entity.getUserId(),
                entity.getMonthlyLimitKwh(),
                true
        );
    }

    public static MonthlyConsumptionThresholdResource toUnconfiguredResource(
            Long userId) {

        return new MonthlyConsumptionThresholdResource(
                userId,
                null,
                false
        );
    }
}
