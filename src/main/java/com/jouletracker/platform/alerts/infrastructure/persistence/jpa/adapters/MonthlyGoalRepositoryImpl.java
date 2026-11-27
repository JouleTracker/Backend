package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyGoalRepository;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers.MonthlyGoalPersistenceAssembler;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.MonthlyGoalPersistenceRepository;
import com.jouletracker.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter implementing {@link MonthlyGoalRepository}.
 */
@Repository
public class MonthlyGoalRepositoryImpl implements MonthlyGoalRepository {

    private final MonthlyGoalPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MonthlyGoalRepositoryImpl(MonthlyGoalPersistenceRepository persistenceRepository,
                                     DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public MonthlyGoal save(MonthlyGoal goal) {
        var entity = MonthlyGoalPersistenceAssembler.toPersistenceFromDomain(goal);
        var saved = persistenceRepository.save(entity);
        var domain = MonthlyGoalPersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClearEvents(goal);
        return domain;
    }

    @Override
    public Optional<MonthlyGoal> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(MonthlyGoalPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<MonthlyGoal> findAllByUserId(Long userId) {
        return persistenceRepository.findAllByUserIdOrderByGoalYearDescGoalMonthDesc(userId).stream()
                .map(MonthlyGoalPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<MonthlyGoal> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month) {
        return persistenceRepository.findByUserIdAndGoalYearAndGoalMonth(userId, year, month)
                .map(MonthlyGoalPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByUserIdAndYearAndMonth(Long userId, Integer year, Integer month) {
        return persistenceRepository.existsByUserIdAndGoalYearAndGoalMonth(userId, year, month);
    }

    @Override
    public void deleteById(Long id) {
        persistenceRepository.deleteById(id);
    }
}