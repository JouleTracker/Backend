package com.jouletracker.platform.devices.domain.model.queries;

/**
 * Query to find all sensors for a user (or all if userId is null).
 */
public record GetAllSensorsByUserIdQuery(Long userId) {
}
