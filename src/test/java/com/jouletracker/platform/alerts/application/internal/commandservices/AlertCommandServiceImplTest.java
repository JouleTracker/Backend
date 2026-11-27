
package com.jouletracker.platform.alerts.application.internal.commandservices;

import com.jouletracker.platform.alerts.application.internal.events.CriticalAlertCreatedEvent;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.commands.EvaluateConsumptionAlertCommand;
import com.jouletracker.platform.alerts.domain.model.commands.MarkAlertAsReadCommand;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertCommandServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private DevicesContextFacade devicesContextFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AlertCommandServiceImpl commandService;

    @Test
    void handleShouldGenerateWarningWhenDailyLimitIsExceeded() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        0.8, 3.5, 3.0, 1.5,
                        1L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                any(), any(), any()))
                .thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> {
                    Alert alert = inv.getArgument(0);
                    alert.setId(1L);
                    return alert;
                });

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 5L,
                        "Refrigeradora", 0.8, 0.5
                )
        );

        assertEquals(1, alerts.size());

        assertEquals(
                AlertSeverity.WARNING,
                alerts.get(0).getSeverity()
        );

        assertTrue(
                alerts.get(0).getTitle()
                        .contains("Límite diario superado")
        );

        assertFalse(alerts.get(0).isRead());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void handleShouldGeneratePreventiveAlertWhenAtLeastEightyPercentOfDailyLimit() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        0.5, 2.5, 3.0, 1.5,
                        1L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                any(), any(), any()))
                .thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 5L,
                        "Refrigeradora", 0.5, 0.2
                )
        );

        assertEquals(1, alerts.size());

        assertEquals(
                AlertSeverity.INFO,
                alerts.get(0).getSeverity()
        );

        assertTrue(
                alerts.get(0).getTitle()
                        .contains("Advertencia preventiva")
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void handleShouldGenerateCriticalPowerAlertWhenPowerLimitExceeded() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Sala",
                        6L, "Aire Acondicionado",
                        2.8, 1.0, 5.0, 2.0,
                        2L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                any(), any(), any()))
                .thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> {
                    Alert alert = inv.getArgument(0);
                    alert.setId(20L);
                    return alert;
                });

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 6L,
                        "Aire Acondicionado", 2.8, 0.4
                )
        );

        assertEquals(1, alerts.size());

        assertEquals(
                AlertSeverity.CRITICAL,
                alerts.get(0).getSeverity()
        );

        assertTrue(
                alerts.get(0).getTitle()
                        .contains("Exceso de potencia detectado")
        );

        verify(eventPublisher, times(1))
                .publishEvent(any(CriticalAlertCreatedEvent.class));
    }

    @Test
    void handleShouldNotGenerateAlertWhenConsumptionIsNormal() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Oficina",
                        7L, "Laptop",
                        0.1, 0.5, 3.0, 1.0,
                        3L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 7L,
                        "Laptop", 0.1, 0.1
                )
        );

        assertTrue(alerts.isEmpty());

        verifyNoInteractions(alertRepository);
        verifyNoInteractions(eventPublisher);
    }

    /**
     * US-32: Prevents alert generation for sensors
     * belonging to another user.
     */
    @Test
    void handleShouldNotGenerateAlertsForAnotherUsersSensor() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 2L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        3.0, 8.0, 3.0, 1.5,
                        1L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 5L,
                        "Refrigeradora", 3.0, 0.5
                )
        );

        assertTrue(alerts.isEmpty());

        verifyNoInteractions(alertRepository);
        verifyNoInteractions(eventPublisher);
    }

    /**
     * TS-19: Verifies that a persisted critical alert
     * publishes an event containing the correct sensor
     * and business area information.
     */
    @Test
    void handleShouldPublishCriticalAlertCreatedEvent() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        3.0, 1.0, 5.0, 1.5,
                        4L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                any(), any(), any()))
                .thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> {
                    Alert alert = inv.getArgument(0);
                    alert.setId(100L);
                    return alert;
                });

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 5L,
                        "Refrigeradora", 3.0, 0.5
                )
        );

        assertEquals(1, alerts.size());

        ArgumentCaptor<CriticalAlertCreatedEvent> captor =
                ArgumentCaptor.forClass(
                        CriticalAlertCreatedEvent.class
                );

        verify(eventPublisher, times(1))
                .publishEvent(captor.capture());

        CriticalAlertCreatedEvent event =
                captor.getValue();

        assertEquals(100L, event.alert().getId());
        assertEquals(1L, event.alert().getUserId());
        assertEquals(10L, event.alert().getSensorId());

        assertEquals(
                AlertSeverity.CRITICAL,
                event.alert().getSeverity()
        );

        assertEquals(
                4L,
                event.snapshot().businessAreaId()
        );

        assertEquals(
                "Sensor Cocina",
                event.snapshot().sensorName()
        );
    }

    /**
     * TS-19: An existing active critical alert must not
     * generate another notification event.
     */
    @Test
    void handleShouldNotPublishEventWhenCriticalAlertAlreadyExists() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Sala",
                        6L, "Aire Acondicionado",
                        3.0, 1.0, 5.0, 2.0,
                        2L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                1L,
                10L,
                "Exceso de potencia detectado: Aire Acondicionado"
        )).thenReturn(true);

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 6L,
                        "Aire Acondicionado", 3.0, 0.5
                )
        );

        assertTrue(alerts.isEmpty());

        verify(alertRepository, never())
                .save(any(Alert.class));

        verifyNoInteractions(eventPublisher);
    }

    /**
     * TS-19: A previous warning must not prevent
     * a new critical daily consumption alert.
     */
    @Test
    void handleShouldAllowWarningToEscalateToCritical() {

        SensorThresholdSnapshot warningSnapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        0.8, 3.5, 3.0, 1.5,
                        1L
                );

        SensorThresholdSnapshot criticalSnapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Cocina",
                        5L, "Refrigeradora",
                        0.8, 4.0, 3.0, 1.5,
                        1L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(
                        Optional.of(warningSnapshot),
                        Optional.of(criticalSnapshot)
                );

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                1L,
                10L,
                "Límite diario superado: Sensor Cocina"
        )).thenReturn(false, true);

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                1L,
                10L,
                "Límite diario superado (crítico): Sensor Cocina"
        )).thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> {
                    Alert alert = inv.getArgument(0);

                    alert.setId(
                            alert.getSeverity() == AlertSeverity.CRITICAL
                                    ? 201L
                                    : 200L
                    );

                    return alert;
                });

        EvaluateConsumptionAlertCommand command =
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 5L,
                        "Refrigeradora", 0.8, 0.5
                );

        List<Alert> warnings = commandService.handle(command);

        assertEquals(1, warnings.size());

        assertEquals(
                AlertSeverity.WARNING,
                warnings.get(0).getSeverity()
        );

        verifyNoInteractions(eventPublisher);

        List<Alert> criticalAlerts =
                commandService.handle(command);

        assertEquals(1, criticalAlerts.size());

        assertEquals(
                AlertSeverity.CRITICAL,
                criticalAlerts.get(0).getSeverity()
        );

        assertTrue(
                criticalAlerts.get(0).getTitle()
                        .contains("(crítico)")
        );

        verify(eventPublisher, times(1))
                .publishEvent(any(CriticalAlertCreatedEvent.class));

        verify(alertRepository, times(2))
                .save(any(Alert.class));
    }

    /**
     * TS-19: Prevents notification events for alerts
     * that do not have a persisted identifier.
     */
    @Test
    void handleShouldNotPublishEventWithoutPersistedAlertId() {

        SensorThresholdSnapshot snapshot =
                new SensorThresholdSnapshot(
                        10L, 1L, "Sensor Sala",
                        6L, "Aire Acondicionado",
                        3.0, 1.0, 5.0, 2.0,
                        2L
                );

        when(devicesContextFacade.fetchSensorThresholdById(10L))
                .thenReturn(Optional.of(snapshot));

        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(
                any(), any(), any()))
                .thenReturn(false);

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(
                        1L, 10L, 6L,
                        "Aire Acondicionado", 3.0, 0.5
                )
        );

        assertEquals(1, alerts.size());
        assertNull(alerts.get(0).getId());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void markAlertAsReadShouldUpdateStatusAndReturnSuccess() {

        Alert existing = new Alert(
                15L,
                1L,
                10L,
                5L,
                "Límite diario superado: Sensor Cocina",
                "Exceso detectado",
                AlertSeverity.WARNING,
                "/dispositivos",
                false,
                AlertStatus.ACTIVE,
                LocalDateTime.now()
        );

        when(alertRepository.findById(15L))
                .thenReturn(Optional.of(existing));

        when(alertRepository.save(any(Alert.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Result<Alert, ApplicationError> result =
                commandService.handle(
                        new MarkAlertAsReadCommand(15L)
                );

        assertTrue(result.isSuccess());

        Alert updated =
                ((Result.Success<Alert, ApplicationError>) result)
                        .value();

        assertTrue(updated.isRead());
        assertEquals(AlertStatus.READ, updated.getStatus());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void markAlertAsReadShouldReturnNotFoundWhenMissing() {

        when(alertRepository.findById(999L))
                .thenReturn(Optional.empty());

        Result<Alert, ApplicationError> result =
                commandService.handle(
                        new MarkAlertAsReadCommand(999L)
                );

        assertTrue(result.isFailure());

        ApplicationError error =
                ((Result.Failure<Alert, ApplicationError>) result)
                        .error();

        assertEquals("ALERT_NOT_FOUND", error.code());

        verifyNoInteractions(eventPublisher);
    }
}
