package com.jouletracker.platform.alerts.domain.model.commands;

/**
 * Command to create a monthly energy consumption goal in kWh (US-28).
 */
public record CreateMonthlyGoalCommand(
        Long userId,
        Double targetKwh,
        Integer year,
        Integer month
) {
}