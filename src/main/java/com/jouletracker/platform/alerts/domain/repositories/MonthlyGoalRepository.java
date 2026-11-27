package com.jouletracker.platform.alerts.domain.repositories;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for MonthlyGoal aggregate (US-28).
 */
public interface MonthlyGoalRepository {
    MonthlyGoal save(MonthlyGoal goal);
    Optional<MonthlyGoal> findById(Long id);
    List<MonthlyGoal> findAllByUserId(Long userId);
    Optional<MonthlyGoal> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    boolean existsByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    void deleteById(Long id);
}