package com.jouletracker.platform.devices.domain.model.queries;

/**
 * Query to find all devices belonging to a user (or all if userId is null).
 */
public record GetAllDevicesByUserIdQuery(Long userId) {
}
