
package com.jouletracker.platform.alerts.infrastructure.persistence;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:monthly_alert_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "application.notifications.critical-email-enabled=false"
})
@ActiveProfiles("test")
class MonthlyConsumptionAlertIntegrationTest {

    @Autowired
    private MonthlyConsumptionThresholdService thresholdService;

    @Autowired
    private EnergyMeasurementRepository measurementRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldGenerateMonthlyAlertAfterTransactionCommits() {

        Long userId = 93001L;

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("150")
        );

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status -> {

            registerMeasurement(
                    userId,
                    100.0,
                    LocalDateTime.now()
            );

            assertTrue(
                    alertRepository
                            .findAllByUserIdOrderByCreatedAtDesc(userId)
                            .isEmpty()
            );
        });

        assertTrue(
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .isEmpty()
        );

        transaction.executeWithoutResult(status -> {

            registerMeasurement(
                    userId,
                    50.0,
                    LocalDateTime.now()
            );

            assertTrue(
                    alertRepository
                            .findAllByUserIdOrderByCreatedAtDesc(userId)
                            .isEmpty()
            );
        });

        var alerts =
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId);

        assertEquals(1, alerts.size());

        var alert = alerts.getFirst();

        assertEquals(userId, alert.getUserId());

        assertEquals(
                AlertSeverity.WARNING,
                alert.getSeverity()
        );

        assertNull(alert.getSensorId());

        assertNull(alert.getDeviceId());

        assertTrue(
                alert.getTitle()
                        .contains(YearMonth.now().toString())
        );

        assertTrue(
                thresholdService
                        .getConsumptionStatus(userId)
                        .thresholdReached()
        );

        // A read alert must not be generated again
        // during the same calendar month.
        alert.markAsRead();

        transaction.executeWithoutResult(status ->
                alertRepository.save(alert)
        );

        transaction.executeWithoutResult(status ->
                registerMeasurement(
                        userId,
                        25.0,
                        LocalDateTime.now()
                )
        );

        assertEquals(
                1,
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .size()
        );
    }

    @Test
    void shouldNotGenerateMonthlyAlertAfterRollback() {

        Long userId = 93002L;

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("100")
        );

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status -> {

            registerMeasurement(
                    userId,
                    150.0,
                    LocalDateTime.now()
            );

            status.setRollbackOnly();
        });

        assertTrue(
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .isEmpty()
        );

        assertEquals(
                0,
                thresholdService
                        .getConsumptionStatus(userId)
                        .currentConsumptionKwh()
                        .compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldNotGenerateAlertWithoutConfiguredThreshold() {

        Long userId = 93003L;

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status ->
                registerMeasurement(
                        userId,
                        500.0,
                        LocalDateTime.now()
                )
        );

        assertTrue(
                thresholdService.getThreshold(userId)
                        .isEmpty()
        );

        assertTrue(
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .isEmpty()
        );
    }

    @Test
    void shouldExcludePreviousMonthAndOtherUsersConsumption() {

        Long userId = 93004L;

        Long anotherUserId = 93005L;

        thresholdService.updateThreshold(
                userId,
                new BigDecimal("100")
        );

        LocalDateTime previousMonth =
                YearMonth.now()
                        .minusMonths(1)
                        .atDay(1)
                        .atTime(12, 0);

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        transaction.executeWithoutResult(status -> {

            registerMeasurement(
                    userId,
                    200.0,
                    previousMonth
            );

            registerMeasurement(
                    anotherUserId,
                    500.0,
                    LocalDateTime.now()
            );

            registerMeasurement(
                    userId,
                    40.0,
                    LocalDateTime.now()
            );
        });

        var consumption =
                thresholdService.getConsumptionStatus(userId);

        assertEquals(
                0,
                consumption.currentConsumptionKwh()
                        .compareTo(new BigDecimal("40"))
        );

        assertFalse(consumption.thresholdReached());

        assertTrue(
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .isEmpty()
        );
    }

    private void registerMeasurement(
            Long userId,
            Double energyKwh,
            LocalDateTime measuredAt) {

        var measurement = new EnergyMeasurement(
                null,
                userId,
                null,
                null,
                "Integration Test Device",
                "Electrodomésticos",
                1.0,
                energyKwh,
                220.0,
                4.55,
                null,
                measuredAt
        );

        measurement.markAsRegistered();

        measurementRepository.save(measurement);
    }
}
