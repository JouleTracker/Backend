
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.MonthlyConsumptionThresholdPersistenceEntity;

/**
 * US-26: Converts monthly consumption thresholds
 * between domain and persistence representations.
 */
public final class MonthlyConsumptionThresholdPersistenceAssembler {

    private MonthlyConsumptionThresholdPersistenceAssembler() {
    }

    public static MonthlyConsumptionThreshold toDomainFromPersistence(
            MonthlyConsumptionThresholdPersistenceEntity entity) {

        return new MonthlyConsumptionThreshold(
                entity.getUserId(),
                entity.getMonthlyLimitKwh()
        );
    }

    public static MonthlyConsumptionThresholdPersistenceEntity toPersistenceFromDomain(
            MonthlyConsumptionThreshold threshold) {

        var entity =
                new MonthlyConsumptionThresholdPersistenceEntity();

        entity.setUserId(
                threshold.getUserId()
        );

        entity.setMonthlyLimitKwh(
                threshold.getMonthlyLimitKwh()
        );

        return entity;
    }
}
