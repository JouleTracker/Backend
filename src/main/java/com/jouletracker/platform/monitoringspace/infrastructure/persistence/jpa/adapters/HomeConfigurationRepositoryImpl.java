
package com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.monitoringspace.domain.repositories.HomeConfigurationRepository;
import com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.assemblers.HomeConfigurationPersistenceAssembler;
import com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.repositories.HomeConfigurationPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * US-48: JPA adapter for the home configuration repository.
 */
@Repository
@Transactional(readOnly = true)
public class HomeConfigurationRepositoryImpl
        implements HomeConfigurationRepository {

    private final HomeConfigurationPersistenceRepository
            persistenceRepository;

    public HomeConfigurationRepositoryImpl(
            HomeConfigurationPersistenceRepository persistenceRepository) {

        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<HomeConfiguration> findByUserId(Long userId) {

        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        return persistenceRepository.findById(userId)
                .map(HomeConfigurationPersistenceAssembler
                        ::toDomainFromPersistence);
    }

    @Override
    @Transactional
    public HomeConfiguration save(
            HomeConfiguration configuration) {

        var entity =
                HomeConfigurationPersistenceAssembler
                        .toPersistenceFromDomain(configuration);

        var saved = persistenceRepository.save(entity);

        return HomeConfigurationPersistenceAssembler
                .toDomainFromPersistence(saved);
    }
}
