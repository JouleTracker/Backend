package com.jouletracker.platform.alerts.domain.model.queries;

/**
 * Query to retrieve all monthly consumption goals for a user (US-28).
 */
public record GetMonthlyGoalsByUserIdQuery(Long userId) {
}