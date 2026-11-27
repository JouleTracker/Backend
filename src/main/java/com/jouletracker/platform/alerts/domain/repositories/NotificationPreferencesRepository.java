
package com.jouletracker.platform.alerts.domain.repositories;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;

import java.util.Optional;

/**
 * US-38: Domain repository port for user
 * notification preferences.
 */
public interface NotificationPreferencesRepository {

    Optional<NotificationPreferences> findByUserId(
            Long userId
    );

    NotificationPreferences save(
            NotificationPreferences preferences
    );
}
