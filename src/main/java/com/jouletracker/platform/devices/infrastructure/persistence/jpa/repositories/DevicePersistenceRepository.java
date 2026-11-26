package com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.DevicePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicePersistenceRepository extends JpaRepository<DevicePersistenceEntity, Long> {
    List<DevicePersistenceEntity> findAllByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
