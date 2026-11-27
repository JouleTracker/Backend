package com.jouletracker.platform.alerts.domain.model.commands;

/**
 * Command to update an existing monthly energy consumption goal (US-28).
 */
public record UpdateMonthlyGoalCommand(
        Long id,
        Long userId,
        Double targetKwh,
        Integer year,
        Integer month
) {
}