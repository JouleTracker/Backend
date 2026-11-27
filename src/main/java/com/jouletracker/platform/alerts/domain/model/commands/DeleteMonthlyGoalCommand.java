package com.jouletracker.platform.alerts.domain.model.commands;

/**
 * Command to delete a monthly energy consumption goal (US-28).
 */
public record DeleteMonthlyGoalCommand(
        Long id,
        Long userId
) {
}