package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.MonthlyGoalPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link MonthlyGoalPersistenceEntity}.
 */
@Repository
public interface MonthlyGoalPersistenceRepository extends JpaRepository<MonthlyGoalPersistenceEntity, Long> {
    List<MonthlyGoalPersistenceEntity> findAllByUserIdOrderByGoalYearDescGoalMonthDesc(Long userId);
    Optional<MonthlyGoalPersistenceEntity> findByUserIdAndGoalYearAndGoalMonth(Long userId, Integer goalYear, Integer goalMonth);
    boolean existsByUserIdAndGoalYearAndGoalMonth(Long userId, Integer goalYear, Integer goalMonth);
}