package com.jouletracker.platform.alerts.domain.model.entities;

/**
 * Read-model combining a MonthlyGoal with actual monthly consumption progress (US-28).
 */
public record MonthlyGoalProgress(
        Long id,
        Long userId,
        Double targetKwh,
        Integer year,
        Integer month,
        Double currentKwh,
        Double progressPercentage,
        Double remainingKwh,
        boolean exceeded
) {
}