package com.jouletracker.platform.devices.interfaces.acl;

import java.util.Optional;

/**
 * Anti-Corruption Layer facade exposing Devices context capabilities to other bounded contexts.
 */
public interface DevicesContextFacade {
    boolean existsSensorById(Long sensorId);
    boolean existsDeviceById(Long deviceId);
    boolean updateSensorTelemetry(Long sensorId, Double powerKw, Double addedKwh, String timestamp);
    Optional<Long> fetchAssignedDeviceIdBySensorId(Long sensorId);
}
