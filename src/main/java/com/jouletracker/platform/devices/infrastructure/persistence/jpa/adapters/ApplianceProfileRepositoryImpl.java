package com.jouletracker.platform.devices.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.domain.repositories.ApplianceProfileRepository;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers.ApplianceProfilePersistenceAssembler;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories.ApplianceProfilePersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ApplianceProfileRepositoryImpl implements ApplianceProfileRepository {

    private final ApplianceProfilePersistenceRepository persistenceRepository;

    public ApplianceProfileRepositoryImpl(ApplianceProfilePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<ApplianceProfile> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(ApplianceProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<ApplianceProfile> findByName(String name) {
        return persistenceRepository.findByName(name)
                .map(ApplianceProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<ApplianceProfile> findAll() {
        return persistenceRepository.findAll().stream()
                .map(ApplianceProfilePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public ApplianceProfile save(ApplianceProfile profile) {
        var entity = ApplianceProfilePersistenceAssembler.toPersistenceFromDomain(profile);
        var savedEntity = persistenceRepository.save(entity);
        return ApplianceProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public long count() {
        return persistenceRepository.count();
    }
}
