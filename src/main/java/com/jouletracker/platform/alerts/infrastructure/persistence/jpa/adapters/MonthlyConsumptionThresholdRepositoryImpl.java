
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyConsumptionThresholdRepository;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers.MonthlyConsumptionThresholdPersistenceAssembler;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.MonthlyConsumptionThresholdPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * US-26: JPA adapter implementing the domain
 * monthly consumption threshold repository.
 */
@Repository
@Transactional(readOnly = true)
public class MonthlyConsumptionThresholdRepositoryImpl
        implements MonthlyConsumptionThresholdRepository {

    private final MonthlyConsumptionThresholdPersistenceRepository
            persistenceRepository;

    public MonthlyConsumptionThresholdRepositoryImpl(
            MonthlyConsumptionThresholdPersistenceRepository
                    persistenceRepository) {

        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<MonthlyConsumptionThreshold> findByUserId(
            Long userId) {

        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        return persistenceRepository.findById(userId)
                .map(MonthlyConsumptionThresholdPersistenceAssembler
                        ::toDomainFromPersistence);
    }

    @Override
    @Transactional
    public MonthlyConsumptionThreshold save(
            MonthlyConsumptionThreshold threshold) {

        var entity =
                MonthlyConsumptionThresholdPersistenceAssembler
                        .toPersistenceFromDomain(threshold);

        var saved =
                persistenceRepository.save(entity);

        return MonthlyConsumptionThresholdPersistenceAssembler
                .toDomainFromPersistence(saved);
    }
}
