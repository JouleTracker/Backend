package com.jouletracker.platform.alerts.interfaces.rest;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.application.queryservices.AlertQueryService;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.commands.MarkAlertAsReadCommand;
import com.jouletracker.platform.alerts.domain.model.entities.AlertSummary;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertSummaryByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertsControllerTest {

    @Mock
    private AlertCommandService alertCommandService;

    @Mock
    private AlertQueryService alertQueryService;

    private MockMvc mockMvc;
    private MockMvc summaryMockMvc;

    @BeforeEach
    void setUp() {
        AlertsController alertsController = new AlertsController(alertCommandService, alertQueryService);
        AlertSummariesController summariesController = new AlertSummariesController(alertQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(alertsController).build();
        summaryMockMvc = MockMvcBuilders.standaloneSetup(summariesController).build();
    }

    @Test
    void getAlertsShouldReturn200WithFrontendCompatibleResources() throws Exception {
        Alert alert = new Alert(
                1L, 1L, 10L, 5L,
                "Límite diario superado: Sensor Cocina",
                "Consumo de 3.50 kWh superó el límite.",
                AlertSeverity.WARNING, "/dispositivos", false, AlertStatus.ACTIVE,
                LocalDateTime.now().minusMinutes(5)
        );
        when(alertQueryService.handle(any(GetAlertsByUserIdQuery.class))).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/v1/alerts").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].severity").value("warning"))
                .andExpect(jsonPath("$[0].status").value("Activa"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].actionUrl").value("/dispositivos"));
    }

    @Test
    void markAlertAsReadShouldReturn200WhenFound() throws Exception {
        Alert readAlert = new Alert(
                1L, 1L, 10L, 5L,
                "Límite diario superado: Sensor Cocina",
                "Consumo de 3.50 kWh superó el límite.",
                AlertSeverity.WARNING, "/dispositivos", true, AlertStatus.READ,
                LocalDateTime.now()
        );
        when(alertCommandService.handle(any(MarkAlertAsReadCommand.class))).thenReturn(Result.success(readAlert));

        mockMvc.perform(patch("/api/v1/alerts/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.status").value("Leída"));
    }

    @Test
    void markAlertAsReadShouldReturn404WhenMissing() throws Exception {
        when(alertCommandService.handle(any(MarkAlertAsReadCommand.class)))
                .thenReturn(Result.failure(ApplicationError.notFound("Alert", "999")));

        mockMvc.perform(patch("/api/v1/alerts/999/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAlertSummariesShouldReturn200WithCounts() throws Exception {
        AlertSummary summary = new AlertSummary(1L, 3L, 2L, 1L);
        when(alertQueryService.handle(any(GetAlertSummaryByUserIdQuery.class))).thenReturn(summary);

        summaryMockMvc.perform(get("/api/v1/alert-summaries").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].totalCount").value(3))
                .andExpect(jsonPath("$[0].activeCount").value(2))
                .andExpect(jsonPath("$[0].resolvedCount").value(1));
    }
}
