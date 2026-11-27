
package com.jouletracker.platform.monitoringspace.infrastructure.persistence;

import com.jouletracker.platform.monitoringspace.application.internal.services.HomeConfigurationService;
import com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.repositories.HomeConfigurationPersistenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:home_configuration_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "application.notifications.critical-email-enabled=false"
})
@ActiveProfiles("test")
class HomeConfigurationPersistenceIntegrationTest {

    @Autowired
    private HomeConfigurationService homeService;

    @Autowired
    private HomeConfigurationPersistenceRepository persistenceRepository;

    @Test
    void shouldPersistHomeConfiguration() {

        Long userId = 94001L;

        homeService.updateConfiguration(
                userId, "Apartment", 4, "Chorrillos", "BT5B"
        );

        var persisted = persistenceRepository
                .findById(userId)
                .orElseThrow();

        assertEquals(userId, persisted.getUserId());
        assertEquals("Apartment", persisted.getHomeType());
        assertEquals(4, persisted.getOccupants());
        assertEquals("Chorrillos", persisted.getLocation());
        assertEquals("BT5B", persisted.getTariff());
    }

    @Test
    void shouldUpdateWithoutCreatingDuplicateRecords() {

        Long userId = 94002L;

        homeService.updateConfiguration(
                userId, "House", 2, "Lima", "BT5B"
        );

        long initialCount = persistenceRepository.count();

        homeService.updateConfiguration(
                userId, "Apartment", 5, "Miraflores", "BT2"
        );

        var persisted = persistenceRepository
                .findById(userId)
                .orElseThrow();

        assertEquals("Apartment", persisted.getHomeType());
        assertEquals(5, persisted.getOccupants());
        assertEquals("Miraflores", persisted.getLocation());
        assertEquals("BT2", persisted.getTariff());

        assertEquals(
                initialCount,
                persistenceRepository.count()
        );
    }

    @Test
    void shouldKeepConfigurationsIndependentBetweenUsers() {

        Long firstUserId = 94003L;
        Long secondUserId = 94004L;

        homeService.updateConfiguration(
                firstUserId, "House", 4, "Lima", "BT5B"
        );

        homeService.updateConfiguration(
                secondUserId, "Apartment", 2, "Cusco", "BT2"
        );

        homeService.updateConfiguration(
                firstUserId, "House", 6, "Chorrillos", "BT5B"
        );

        var first = homeService
                .getConfiguration(firstUserId)
                .orElseThrow();

        var second = homeService
                .getConfiguration(secondUserId)
                .orElseThrow();

        assertEquals(6, first.getOccupants());
        assertEquals("Chorrillos", first.getLocation());

        assertEquals(2, second.getOccupants());
        assertEquals("Cusco", second.getLocation());
        assertEquals("Apartment", second.getHomeType());
    }

    @Test
    void shouldNotAutomaticallyPersistDefaultConfiguration() {

        Long userId = 94005L;

        assertFalse(persistenceRepository.existsById(userId));

        assertTrue(
                homeService.getConfiguration(userId).isEmpty()
        );

        assertFalse(persistenceRepository.existsById(userId));
    }

    @Test
    void shouldRejectInvalidConfigurationWithoutPersistingIt() {

        Long userId = 94006L;

        assertThrows(
                IllegalArgumentException.class,
                () -> homeService.updateConfiguration(
                        userId, "House", 0, "Lima", "BT5B"
                )
        );

        assertFalse(persistenceRepository.existsById(userId));
    }
}
