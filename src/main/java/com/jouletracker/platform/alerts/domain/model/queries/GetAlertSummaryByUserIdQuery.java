package com.jouletracker.platform.alerts.domain.model.queries;

/**
 * Query to retrieve aggregated alert counts for a user.
 */
public record GetAlertSummaryByUserIdQuery(Long userId) {
}
