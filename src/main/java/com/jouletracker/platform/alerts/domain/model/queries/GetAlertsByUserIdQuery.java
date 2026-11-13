package com.jouletracker.platform.alerts.domain.model.queries;

/**
 * Query to retrieve consumption alerts belonging to a user.
 */
public record GetAlertsByUserIdQuery(Long userId) {
}
