
package com.jouletracker.platform.alerts.infrastructure.persistence;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.MonthlyConsumptionThresholdPersistenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:monthly_threshold_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "application.notifications.critical-email-enabled=false"
})
@ActiveProfiles("test")
class MonthlyConsumptionThresholdPersistenceIntegrationTest {

    @Autowired
    private MonthlyConsumptionThresholdService thresholdService;

    @Autowired
    private MonthlyConsumptionThresholdPersistenceRepository
            persistenceRepository;

    @Test
    void shouldPersistMonthlyThreshold() {

        Long userId = 92001L;

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("150.125")
        );

        var persisted =
                persistenceRepository.findById(userId)
                        .orElseThrow();

        assertEquals(userId, persisted.getUserId());

        assertEquals(
                new BigDecimal("150.125"),
                persisted.getMonthlyLimitKwh()
        );

        var retrieved =
                thresholdService.getThreshold(userId)
                        .orElseThrow();

        assertEquals(
                new BigDecimal("150.125"),
                retrieved.getMonthlyLimitKwh()
        );
    }

    @Test
    void shouldUpdateThresholdWithoutCreatingDuplicateRecords() {

        Long userId = 92002L;

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("150")
        );

        long initialCount =
                persistenceRepository.count();

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("250")
        );

        var persisted =
                persistenceRepository.findById(userId)
                        .orElseThrow();

        assertEquals(
                new BigDecimal("250.000"),
                persisted.getMonthlyLimitKwh()
        );

        assertEquals(
                initialCount,
                persistenceRepository.count()
        );
    }

    @Test
    void shouldKeepThresholdsIndependentBetweenUsers() {

        Long firstUserId = 92003L;
        Long secondUserId = 92004L;

        thresholdService.updateThreshold(
                firstUserId,
                new BigDecimal("100")
        );

        thresholdService.updateThreshold(
                secondUserId,
                new BigDecimal("300")
        );

        thresholdService.updateThreshold(
                firstUserId,
                new BigDecimal("200")
        );

        var firstThreshold =
                thresholdService.getThreshold(firstUserId)
                        .orElseThrow();

        var secondThreshold =
                thresholdService.getThreshold(secondUserId)
                        .orElseThrow();

        assertEquals(
                new BigDecimal("200.000"),
                firstThreshold.getMonthlyLimitKwh()
        );

        assertEquals(
                new BigDecimal("300.000"),
                secondThreshold.getMonthlyLimitKwh()
        );
    }

    @Test
    void shouldNotCreateThresholdAutomatically() {

        Long userId = 92005L;

        assertFalse(
                persistenceRepository.existsById(userId)
        );

        assertTrue(
                thresholdService.getThreshold(userId)
                        .isEmpty()
        );

        assertFalse(
                persistenceRepository.existsById(userId)
        );
    }

    @Test
    void shouldRejectInvalidThresholdWithoutPersistingIt() {

        Long userId = 92006L;

        assertThrows(
                IllegalArgumentException.class,
                () -> thresholdService.updateThreshold(
                        userId,
                        BigDecimal.ZERO
                )
        );

        assertFalse(
                persistenceRepository.existsById(userId)
        );
    }
}
