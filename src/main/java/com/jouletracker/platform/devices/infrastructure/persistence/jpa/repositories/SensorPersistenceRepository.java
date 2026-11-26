package com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.SensorPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorPersistenceRepository extends JpaRepository<SensorPersistenceEntity, Long> {
    Optional<SensorPersistenceEntity> findBySerialNumber(String serialNumber);
    List<SensorPersistenceEntity> findAllByUserId(Long userId);
    List<SensorPersistenceEntity> findByAssignedDeviceId(Long assignedDeviceId);
    boolean existsBySerialNumber(String serialNumber);
}
