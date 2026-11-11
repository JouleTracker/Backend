package com.jouletracker.platform.devices.domain.model.queries;

/**
 * Query to calculate aggregated metrics for devices.
 */
public record GetDeviceSummaryByUserIdQuery(Long userId) {
}
