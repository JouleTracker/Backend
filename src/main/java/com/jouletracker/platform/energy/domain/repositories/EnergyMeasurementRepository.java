package com.jouletracker.platform.energy.domain.repositories;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for managing EnergyMeasurement aggregates.
 */
public interface EnergyMeasurementRepository {
    EnergyMeasurement save(EnergyMeasurement measurement);
    Optional<EnergyMeasurement> findById(Long id);
    List<EnergyMeasurement> findAllByUserId(Long userId);
    List<EnergyMeasurement> findAllByUserIdAndMeasuredAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<EnergyMeasurement> findAllByUserIdAndDeviceIdAndMeasuredAtBetween(Long userId, Long deviceId, LocalDateTime start, LocalDateTime end);
    Optional<EnergyMeasurement> findTopBySensorIdOrderByMeasuredAtDesc(Long sensorId);
}