
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.alerts.domain.repositories.NotificationPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * US-38: Application service for persistent
 * user notification preferences.
 */
@Service
@Transactional(readOnly = true)
public class NotificationPreferencesService {

    private final NotificationPreferencesRepository
            preferencesRepository;

    public NotificationPreferencesService(
            NotificationPreferencesRepository preferencesRepository) {

        this.preferencesRepository = preferencesRepository;
    }

    /**
     * Retrieves saved preferences or returns default values
     * when the user has not configured them yet.
     */
    public NotificationPreferences getPreferences(Long userId) {

        validateUserId(userId);

        return preferencesRepository.findByUserId(userId)
                .orElseGet(() -> new NotificationPreferences(userId));
    }

    /**
     * Creates or updates the notification preferences.
     */
    @Transactional
    public NotificationPreferences updatePreferences(
            Long userId,
            boolean emailEnabled,
            boolean whatsappEnabled) {

        validateUserId(userId);

        NotificationPreferences preferences =
                preferencesRepository.findByUserId(userId)
                        .orElseGet(() ->
                                new NotificationPreferences(userId)
                        );

        preferences.updatePreferences(
                emailEnabled,
                whatsappEnabled
        );

        return preferencesRepository.save(preferences);
    }

    /**
     * Determines whether critical email notifications
     * are enabled for the specified user.
     */
    public boolean isEmailEnabled(Long userId) {

        return getPreferences(userId).isEmailEnabled();
    }

    /**
     * Determines whether WhatsApp notifications are enabled.
     */
    public boolean isWhatsappEnabled(Long userId) {

        return getPreferences(userId).isWhatsappEnabled();
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }
    }
}
