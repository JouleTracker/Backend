package com.jouletracker.platform.devices.domain.model.queries;

/**
 * Query to calculate aggregated metrics for sensors.
 */
public record GetSensorSummaryByUserIdQuery(Long userId) {
}
