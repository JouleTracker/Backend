package com.jouletracker.platform.alerts.domain.model.queries;

/**
 * Query to retrieve active (unread) alerts for a user, optionally filtered by severity (US-27).
 */
public record GetActiveAlertsByUserIdQuery(Long userId, String severity) {
}