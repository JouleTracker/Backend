package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.MonthlyGoalPersistenceEntity;

/**
 * Assembler converting between MonthlyGoal domain aggregate and JPA entity.
 */
public final class MonthlyGoalPersistenceAssembler {

    private MonthlyGoalPersistenceAssembler() {
    }

    public static MonthlyGoal toDomainFromPersistence(MonthlyGoalPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new MonthlyGoal(
                entity.getId(),
                entity.getUserId(),
                entity.getTargetKwh(),
                entity.getGoalYear(),
                entity.getGoalMonth()
        );
    }

    public static MonthlyGoalPersistenceEntity toPersistenceFromDomain(MonthlyGoal domain) {
        if (domain == null) {
            return null;
        }
        var entity = new MonthlyGoalPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTargetKwh(domain.getTargetKwh());
        entity.setGoalYear(domain.getYear());
        entity.setGoalMonth(domain.getMonth());
        return entity;
    }
}