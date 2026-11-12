package com.jouletracker.platform.energy.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.energy.infrastructure.persistence.jpa.entities.EnergyMeasurementPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnergyMeasurementPersistenceRepository extends JpaRepository<EnergyMeasurementPersistenceEntity, Long> {
    List<EnergyMeasurementPersistenceEntity> findAllByUserIdOrderByMeasuredAtDesc(Long userId);
    List<EnergyMeasurementPersistenceEntity> findAllByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(Long userId, LocalDateTime start, LocalDateTime end);
    List<EnergyMeasurementPersistenceEntity> findAllByUserIdAndDeviceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(Long userId, Long deviceId, LocalDateTime start, LocalDateTime end);
}