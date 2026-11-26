package com.jouletracker.platform.alerts.domain.model.entities;

/**
 * Domain read-model summarizing alert counts for a user.
 */
public record AlertSummary(
        Long userId,
        long totalCount,
        long activeCount,
        long resolvedCount
) {
}
