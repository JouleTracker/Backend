
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.NotificationPreferencesPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * US-38: Spring Data JPA repository for
 * notification preferences persistence.
 */
@Repository
public interface NotificationPreferencesPersistenceRepository
        extends JpaRepository<NotificationPreferencesPersistenceEntity, Long> {
}
