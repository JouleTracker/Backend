
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.alerts.domain.repositories.NotificationPreferencesRepository;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers.NotificationPreferencesPersistenceAssembler;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.NotificationPreferencesPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * US-38: JPA adapter implementing the domain
 * notification preferences repository.
 */
@Repository
@Transactional(readOnly = true)
public class NotificationPreferencesRepositoryImpl
        implements NotificationPreferencesRepository {

    private final NotificationPreferencesPersistenceRepository
            persistenceRepository;

    public NotificationPreferencesRepositoryImpl(
            NotificationPreferencesPersistenceRepository
                    persistenceRepository) {

        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<NotificationPreferences> findByUserId(
            Long userId) {

        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        return persistenceRepository.findById(userId)
                .map(NotificationPreferencesPersistenceAssembler
                        ::toDomainFromPersistence);
    }

    @Override
    @Transactional
    public NotificationPreferences save(
            NotificationPreferences preferences) {

        var entity =
                NotificationPreferencesPersistenceAssembler
                        .toPersistenceFromDomain(preferences);

        var saved = persistenceRepository.save(entity);

        return NotificationPreferencesPersistenceAssembler
                .toDomainFromPersistence(saved);
    }
}
