package com.jouletracker.platform.devices.domain.model.events;

/**
 * Domain event dispatched when a new IoT sensor is registered.
 */
public record SensorRegisteredEvent(Long sensorId, Long userId, String serialNumber) {
}
