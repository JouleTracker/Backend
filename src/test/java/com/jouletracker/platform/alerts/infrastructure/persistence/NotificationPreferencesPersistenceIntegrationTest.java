
package com.jouletracker.platform.alerts.infrastructure.persistence;

import com.jouletracker.platform.alerts.application.internal.services.NotificationPreferencesService;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.NotificationPreferencesPersistenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * US-38: Integration tests for notification
 * preferences using a real H2 test database.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification_preferences_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "application.notifications.critical-email-enabled=false"
})
@ActiveProfiles("test")
class NotificationPreferencesPersistenceIntegrationTest {

    @Autowired
    private NotificationPreferencesService preferencesService;

    @Autowired
    private NotificationPreferencesPersistenceRepository
            persistenceRepository;

    @Test
    void shouldPersistNotificationPreferences() {

        Long userId = 91001L;

        preferencesService.updatePreferences(
                userId,
                true,
                false
        );

        var persisted =
                persistenceRepository.findById(userId)
                        .orElseThrow();

        assertEquals(userId, persisted.getUserId());

        assertTrue(persisted.isEmailEnabled());

        assertFalse(persisted.isWhatsappEnabled());

        var retrieved =
                preferencesService.getPreferences(userId);

        assertTrue(retrieved.isEmailEnabled());

        assertFalse(retrieved.isWhatsappEnabled());
    }

    @Test
    void shouldUpdatePreferencesWithoutCreatingDuplicateRecords() {

        Long userId = 91002L;

        preferencesService.updatePreferences(
                userId,
                true,
                true
        );

        long initialCount =
                persistenceRepository.count();

        preferencesService.updatePreferences(
                userId,
                false,
                true
        );

        var persisted =
                persistenceRepository.findById(userId)
                        .orElseThrow();

        assertFalse(persisted.isEmailEnabled());

        assertTrue(persisted.isWhatsappEnabled());

        assertEquals(
                initialCount,
                persistenceRepository.count()
        );
    }

    @Test
    void shouldKeepPersistedPreferencesIndependentBetweenUsers() {

        Long firstUserId = 91003L;

        Long secondUserId = 91004L;

        preferencesService.updatePreferences(
                firstUserId,
                true,
                false
        );

        preferencesService.updatePreferences(
                secondUserId,
                false,
                true
        );

        var firstUser =
                preferencesService.getPreferences(
                        firstUserId
                );

        var secondUser =
                preferencesService.getPreferences(
                        secondUserId
                );

        assertEquals(firstUserId, firstUser.getUserId());

        assertTrue(firstUser.isEmailEnabled());

        assertFalse(firstUser.isWhatsappEnabled());

        assertEquals(secondUserId, secondUser.getUserId());

        assertFalse(secondUser.isEmailEnabled());

        assertTrue(secondUser.isWhatsappEnabled());

        preferencesService.updatePreferences(
                firstUserId,
                false,
                false
        );

        var unchangedSecondUser =
                preferencesService.getPreferences(
                        secondUserId
                );

        assertFalse(
                unchangedSecondUser.isEmailEnabled()
        );

        assertTrue(
                unchangedSecondUser.isWhatsappEnabled()
        );
    }

    @Test
    void shouldReturnDefaultPreferencesWithoutPersistingNewRecord() {

        Long userId = 91005L;

        assertFalse(
                persistenceRepository.existsById(userId)
        );

        var preferences =
                preferencesService.getPreferences(userId);

        assertEquals(userId, preferences.getUserId());

        assertFalse(preferences.isEmailEnabled());

        assertFalse(preferences.isWhatsappEnabled());

        assertFalse(
                persistenceRepository.existsById(userId)
        );
    }
}
