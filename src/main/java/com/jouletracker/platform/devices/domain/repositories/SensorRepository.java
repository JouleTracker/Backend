package com.jouletracker.platform.devices.domain.repositories;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for Sensor aggregate.
 */
public interface SensorRepository {
    Optional<Sensor> findById(Long id);
    Optional<Sensor> findBySerialNumber(SerialNumber serialNumber);
    List<Sensor> findAllByUserId(Long userId);
    List<Sensor> findAll();
    List<Sensor> findByAssignedDeviceId(Long deviceId);
    Sensor save(Sensor sensor);
    void deleteById(Long id);
    boolean existsBySerialNumber(SerialNumber serialNumber);
}
