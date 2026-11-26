package com.jouletracker.platform.businessenergy.domain.model.queries;

/**
 * Query to retrieve energy consumption metrics for a specific physical business area (TS-17).
 */
public record GetBusinessAreaMetricsByIdQuery(Long areaId, String period) {
}
