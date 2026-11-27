
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.MonthlyConsumptionThresholdPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * US-26: Spring Data JPA repository for
 * monthly consumption thresholds.
 */
@Repository
public interface MonthlyConsumptionThresholdPersistenceRepository
        extends JpaRepository<
        MonthlyConsumptionThresholdPersistenceEntity,
        Long> {
}
