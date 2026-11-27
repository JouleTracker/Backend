package com.jouletracker.platform.alerts.domain.model.queries;

/**
 * Query to retrieve the monthly consumption goal for a user and specific year/month (US-28).
 */
public record GetCurrentMonthlyGoalQuery(Long userId, Integer year, Integer month) {
}