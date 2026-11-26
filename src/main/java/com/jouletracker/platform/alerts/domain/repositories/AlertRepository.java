package com.jouletracker.platform.alerts.domain.repositories;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for Alert aggregate.
 */
public interface AlertRepository {
    Alert save(Alert alert);
    Optional<Alert> findById(Long id);
    List<Alert> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsActiveByUserIdAndSensorIdAndTitle(Long userId, Long sensorId, String title);
}
