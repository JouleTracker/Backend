package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AlertPersistenceEntity}.
 */
@Repository
public interface AlertPersistenceRepository extends JpaRepository<AlertPersistenceEntity, Long> {
    List<AlertPersistenceEntity> findAllByUserIdOrderByTriggeredAtDesc(Long userId);
    boolean existsByUserIdAndSensorIdAndTitleAndReadFalse(Long userId, Long sensorId, String title);
    boolean existsByUserIdAndSensorIdIsNullAndTitleAndReadFalse(Long userId, String title);
}
