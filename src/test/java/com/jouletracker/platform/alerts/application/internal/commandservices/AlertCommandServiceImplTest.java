package com.jouletracker.platform.alerts.application.internal.commandservices;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private AlertCommandServiceImpl commandService;

    @Test
    void handleShouldGenerateWarningWhenDailyLimitIsExceeded() {
        SensorThresholdSnapshot snapshot = new SensorThresholdSnapshot(
                10L, 1L, "Sensor Cocina", 5L, "Refrigeradora",
                0.8, 3.5, 3.0, 1.5
        );
        when(devicesContextFacade.fetchSensorThresholdById(10L)).thenReturn(Optional.of(snapshot));
        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> {
            Alert a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(1L, 10L, 5L, "Refrigeradora", 0.8, 0.5)
        );

        assertEquals(1, alerts.size());
        assertEquals(AlertSeverity.WARNING, alerts.get(0).getSeverity());
        assertTrue(alerts.get(0).getTitle().contains("Límite diario superado"));
        assertFalse(alerts.get(0).isRead());
    }

    @Test
    void handleShouldGeneratePreventiveAlertWhenAtLeastEightyPercentOfDailyLimit() {
        SensorThresholdSnapshot snapshot = new SensorThresholdSnapshot(
                10L, 1L, "Sensor Cocina", 5L, "Refrigeradora",
                0.5, 2.5, 3.0, 1.5
        );
        when(devicesContextFacade.fetchSensorThresholdById(10L)).thenReturn(Optional.of(snapshot));
        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(1L, 10L, 5L, "Refrigeradora", 0.5, 0.2)
        );

        assertEquals(1, alerts.size());
        assertEquals(AlertSeverity.INFO, alerts.get(0).getSeverity());
        assertTrue(alerts.get(0).getTitle().contains("Advertencia preventiva"));
    }

    @Test
    void handleShouldGenerateCriticalPowerAlertWhenPowerLimitExceeded() {
        SensorThresholdSnapshot snapshot = new SensorThresholdSnapshot(
                10L, 1L, "Sensor Sala", 6L, "Aire Acondicionado",
                2.8, 1.0, 5.0, 2.0
        );
        when(devicesContextFacade.fetchSensorThresholdById(10L)).thenReturn(Optional.of(snapshot));
        when(alertRepository.existsActiveByUserIdAndSensorIdAndTitle(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(1L, 10L, 6L, "Aire Acondicionado", 2.8, 0.4)
        );

        assertEquals(1, alerts.size());
        assertEquals(AlertSeverity.CRITICAL, alerts.get(0).getSeverity());
        assertTrue(alerts.get(0).getTitle().contains("Exceso de potencia detectado"));
    }

    @Test
    void handleShouldNotGenerateAlertWhenConsumptionIsNormal() {
        SensorThresholdSnapshot snapshot = new SensorThresholdSnapshot(
                10L, 1L, "Sensor Oficina", 7L, "Laptop",
                0.1, 0.5, 3.0, 1.0
        );
        when(devicesContextFacade.fetchSensorThresholdById(10L)).thenReturn(Optional.of(snapshot));

        List<Alert> alerts = commandService.handle(
                new EvaluateConsumptionAlertCommand(1L, 10L, 7L, "Laptop", 0.1, 0.1)
        );

        assertTrue(alerts.isEmpty());
        verifyNoInteractions(alertRepository);
    }

    @Test
    void markAlertAsReadShouldUpdateStatusAndReturnSuccess() {
        Alert existing = new Alert(
                15L, 1L, 10L, 5L, "Límite diario superado: Sensor Cocina",
                "Exceso detectado", AlertSeverity.WARNING, "/dispositivos",
                false, AlertStatus.ACTIVE, LocalDateTime.now()
        );
        when(alertRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<Alert, ApplicationError> result = commandService.handle(new MarkAlertAsReadCommand(15L));

        assertTrue(result.isSuccess());
        Alert updated = ((Result.Success<Alert, ApplicationError>) result).value();
        assertTrue(updated.isRead());
        assertEquals(AlertStatus.READ, updated.getStatus());
    }

    @Test
    void markAlertAsReadShouldReturnNotFoundWhenMissing() {
        when(alertRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Alert, ApplicationError> result = commandService.handle(new MarkAlertAsReadCommand(999L));

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<Alert, ApplicationError>) result).error();
        assertEquals("ALERT_NOT_FOUND", error.code());
    }
}
