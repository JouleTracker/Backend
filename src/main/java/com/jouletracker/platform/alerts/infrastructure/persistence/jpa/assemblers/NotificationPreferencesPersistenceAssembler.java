
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.NotificationPreferencesPersistenceEntity;

/**
 * US-38: Converts notification preferences between
 * domain and persistence representations.
 */
public final class NotificationPreferencesPersistenceAssembler {

    private NotificationPreferencesPersistenceAssembler() {
    }

    public static NotificationPreferences toDomainFromPersistence(
            NotificationPreferencesPersistenceEntity entity) {

        return new NotificationPreferences(
                entity.getUserId(),
                entity.isEmailEnabled(),
                entity.isWhatsappEnabled()
        );
    }

    public static NotificationPreferencesPersistenceEntity toPersistenceFromDomain(
            NotificationPreferences preferences) {

        var entity =
                new NotificationPreferencesPersistenceEntity();

        entity.setUserId(preferences.getUserId());

        entity.setEmailEnabled(
                preferences.isEmailEnabled()
        );

        entity.setWhatsappEnabled(
                preferences.isWhatsappEnabled()
        );

        return entity;
    }
}
