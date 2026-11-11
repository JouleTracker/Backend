package com.jouletracker.platform.devices.domain.model.events;

/**
 * Domain event dispatched when a device is deleted.
 */
public record DeviceDeletedEvent(Long deviceId, Long userId) {
}
