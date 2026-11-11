package com.jouletracker.platform.devices.domain.repositories;

import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for ApplianceProfile entity.
 */
public interface ApplianceProfileRepository {
    Optional<ApplianceProfile> findById(Long id);
    Optional<ApplianceProfile> findByName(String name);
    List<ApplianceProfile> findAll();
    ApplianceProfile save(ApplianceProfile profile);
    long count();
}
