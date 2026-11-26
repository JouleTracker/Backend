package com.jouletracker.platform.businessenergy.domain.model.queries;

/**
 * Query to retrieve aggregated dashboard energy metrics across all physical areas of a user (TS-17).
 */
public record GetBusinessDashboardMetricsQuery(Long userId, String period) {
}
