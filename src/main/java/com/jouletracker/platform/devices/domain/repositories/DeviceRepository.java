package com.jouletracker.platform.devices.domain.repositories;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for Device aggregate.
 */
public interface DeviceRepository {
    Optional<Device> findById(Long id);
    List<Device> findAllByUserId(Long userId);
    List<Device> findAll();
    Device save(Device device);
    void deleteById(Long id);
    boolean existsByUserIdAndName(Long userId, String name);
}
