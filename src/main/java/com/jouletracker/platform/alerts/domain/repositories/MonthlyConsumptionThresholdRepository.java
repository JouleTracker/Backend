
package com.jouletracker.platform.alerts.domain.repositories;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;

import java.util.Optional;

/**
 * US-26: Domain repository port for global
 * monthly consumption thresholds.
 */
public interface MonthlyConsumptionThresholdRepository {

    Optional<MonthlyConsumptionThreshold> findByUserId(
            Long userId
    );

    MonthlyConsumptionThreshold save(
            MonthlyConsumptionThreshold threshold
    );
}
