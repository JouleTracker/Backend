package com.jouletracker.platform.alerts.application.internal.queryservices;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.queries.GetActiveAlertsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertQueryServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertCommandService alertCommandService;

    @InjectMocks
    private AlertQueryServiceImpl queryService;

    @Test
    void handleActiveAlertsShouldFilterUnreadAndCriticalSeverity() {
        Alert activeCritical = new Alert(
                1L, 1L, 10L, 5L, "Power excess detected: AC",
                "Exceeds limit", AlertSeverity.CRITICAL, "/dispositivos",
                false, AlertStatus.ACTIVE, LocalDateTime.now()
        );
        Alert readCritical = new Alert(
                2L, 1L, 10L, 5L, "Old critical",
                "Already read", AlertSeverity.CRITICAL, "/dispositivos",
                true, AlertStatus.READ, LocalDateTime.now().minusHours(2)
        );
        Alert activeWarning = new Alert(
                3L, 1L, 10L, 5L, "Daily limit exceeded",
                "Warning", AlertSeverity.WARNING, "/dispositivos",
                false, AlertStatus.ACTIVE, LocalDateTime.now().minusMinutes(10)
        );

        when(alertRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activeCritical, readCritical, activeWarning));

        List<Alert> result = queryService.handle(new GetActiveAlertsByUserIdQuery(1L, "critical"));

        verify(alertCommandService).evaluateUserSensors(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(AlertSeverity.CRITICAL, result.get(0).getSeverity());
    }
}