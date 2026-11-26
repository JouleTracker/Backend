package com.jouletracker.platform.energy.domain.model.queries;

/**
 * Query to retrieve the latest recorded energy measurement for a given IoT sensor (TS-10).
 *
 * @param sensorId the IoT sensor ID
 */
public record GetLatestReadingBySensorIdQuery(Long sensorId) {
}
