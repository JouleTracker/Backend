package com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities.ApplianceProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplianceProfilePersistenceRepository extends JpaRepository<ApplianceProfilePersistenceEntity, Long> {
    Optional<ApplianceProfilePersistenceEntity> findByName(String name);
}
