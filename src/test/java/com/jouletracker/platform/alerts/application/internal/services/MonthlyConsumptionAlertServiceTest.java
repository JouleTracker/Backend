
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionThresholdService.MonthlyConsumptionStatus;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyConsumptionAlertServiceTest {

    private static final String PERIOD = "2026-11";

    private static final String ALERT_TITLE =
            "Límite mensual de consumo alcanzado: " + PERIOD;

    @Mock
    private MonthlyConsumptionThresholdService thresholdService;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private MonthlyConsumptionAlertService alertService;

    @Test
    void shouldNotGenerateAlertForInvalidUser() {

        assertTrue(
                alertService.evaluateMonthlyConsumption(0L)
                        .isEmpty()
        );

        verifyNoInteractions(
                thresholdService,
                alertRepository
        );
    }

    @Test
    void shouldNotGenerateAlertWithoutConfiguredThreshold() {

        when(thresholdService.getThreshold(1L))
                .thenReturn(Optional.empty());

        assertTrue(
                alertService.evaluateMonthlyConsumption(1L)
                        .isEmpty()
        );

        verifyNoInteractions(alertRepository);
    }

    @Test
    void shouldNotGenerateAlertBelowThreshold() {

        mockConsumptionStatus("120");

        assertTrue(
                alertService.evaluateMonthlyConsumption(1L)
                        .isEmpty()
        );

        verifyNoInteractions(alertRepository);
    }

    @Test
    void shouldGenerateAlertWhenThresholdIsReached() {

        mockConsumptionStatus("150");

        when(alertRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        mockAlertPersistence();

        var result = alertService
                .evaluateMonthlyConsumption(1L)
                .orElseThrow();

        assertEquals(1L, result.getUserId());

        assertEquals(ALERT_TITLE, result.getTitle());

        assertEquals(
                AlertSeverity.WARNING,
                result.getSeverity()
        );

        assertEquals(
                AlertStatus.ACTIVE,
                result.getStatus()
        );

        assertFalse(result.isRead());

        assertNull(result.getSensorId());

        assertNull(result.getDeviceId());

        assertEquals(
                "/configuracion",
                result.getActionUrl()
        );

        verify(alertRepository)
                .save(any(Alert.class));
    }

    @Test
    void shouldGenerateAlertWhenThresholdIsExceeded() {

        mockConsumptionStatus("165");

        when(alertRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        mockAlertPersistence();

        var alert = alertService
                .evaluateMonthlyConsumption(1L)
                .orElseThrow();

        assertTrue(
                alert.getDescription().contains("165 kWh")
        );

        assertTrue(
                alert.getDescription().contains("110.00%")
        );

        verify(alertRepository)
                .save(any(Alert.class));
    }

    @Test
    void shouldNotGenerateDuplicateMonthlyAlert() {

        mockConsumptionStatus("165");

        when(alertRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        createExistingAlert(PERIOD, false)
                ));

        assertTrue(
                alertService.evaluateMonthlyConsumption(1L)
                        .isEmpty()
        );

        verify(alertRepository, never())
                .save(any(Alert.class));
    }

    @Test
    void shouldNotRepeatAlertAlreadyMarkedAsRead() {

        mockConsumptionStatus("180");

        when(alertRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        createExistingAlert(PERIOD, true)
                ));

        assertTrue(
                alertService.evaluateMonthlyConsumption(1L)
                        .isEmpty()
        );

        verify(alertRepository, never())
                .save(any(Alert.class));
    }

    @Test
    void shouldAllowAlertForNewMonth() {

        mockConsumptionStatus("160");

        when(alertRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(
                        createExistingAlert("2026-10", true)
                ));

        mockAlertPersistence();

        var alert = alertService
                .evaluateMonthlyConsumption(1L)
                .orElseThrow();

        assertEquals(ALERT_TITLE, alert.getTitle());

        verify(alertRepository)
                .save(any(Alert.class));
    }

    private void mockConsumptionStatus(String consumption) {

        var threshold = new MonthlyConsumptionThreshold(
                1L,
                new BigDecimal("150")
        );

        BigDecimal current =
                new BigDecimal(consumption);

        BigDecimal limit =
                threshold.getMonthlyLimitKwh();

        when(thresholdService.getThreshold(1L))
                .thenReturn(Optional.of(threshold));

        when(thresholdService.getConsumptionStatus(
                eq(1L),
                any(LocalDateTime.class)
        )).thenReturn(
                new MonthlyConsumptionStatus(
                        1L,
                        PERIOD,
                        true,
                        limit,
                        current,
                        limit.subtract(current)
                                .max(BigDecimal.ZERO),
                        threshold.calculateConsumptionPercentage(
                                current
                        ),
                        threshold.isThresholdReached(current)
                )
        );
    }

    private void mockAlertPersistence() {

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(invocation -> {

                    Alert alert = invocation.getArgument(0);

                    alert.setId(100L);

                    return alert;
                });
    }

    private Alert createExistingAlert(
            String period,
            boolean read) {

        return new Alert(
                50L,
                1L,
                null,
                null,
                "Límite mensual de consumo alcanzado: " + period,
                "Monthly consumption alert",
                AlertSeverity.WARNING,
                "/configuracion",
                read,
                read ? AlertStatus.READ : AlertStatus.ACTIVE,
                LocalDateTime.parse(period + "-01T12:00:00")
        );
    }
}
