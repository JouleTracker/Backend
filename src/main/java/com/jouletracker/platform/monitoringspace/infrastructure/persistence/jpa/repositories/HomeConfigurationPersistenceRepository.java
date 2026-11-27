
package com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.entities.HomeConfigurationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * US-48: Spring Data JPA repository
 * for home configurations.
 */
@Repository
public interface HomeConfigurationPersistenceRepository
        extends JpaRepository<
        HomeConfigurationPersistenceEntity,
        Long> {
}
