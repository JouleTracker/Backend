package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionHistoryQueryService;
import com.jouletracker.platform.energy.domain.model.entities.ConsumptionHistory;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConsumptionHistoriesControllerTest {

    @Mock
    private ConsumptionHistoryQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ConsumptionHistoriesController(queryService)).build();
    }

    @Test
    void getConsumptionHistoryShouldParseDateRangeAndReturn200() throws Exception {
        ConsumptionHistory row = new ConsumptionHistory(
                1L, 5L, "HVAC Unit", "Cooling", 1.85, 3.2, 2.24, "Normal",
                LocalDateTime.of(2026, 11, 15, 14, 30)
        );
        when(queryService.handle(any(GetConsumptionHistoryQuery.class))).thenReturn(List.of(row));

        mockMvc.perform(get("/api/v1/consumption-histories")
                        .param("userId", "1")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceName").value("HVAC Unit"))
                .andExpect(jsonPath("$[0].status").value("Normal"));

        ArgumentCaptor<GetConsumptionHistoryQuery> captor = ArgumentCaptor.forClass(GetConsumptionHistoryQuery.class);
        verify(queryService).handle(captor.capture());
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), captor.getValue().from());
        assertEquals(LocalDateTime.of(2026, 11, 30, 23, 59, 59), captor.getValue().to());
    }

    @Test
    void getConsumptionHistoryShouldReturn400WhenDateRangeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/consumption-histories")
                        .param("from", "2026-11-20")
                        .param("to", "2026-11-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getConsumptionHistoryShouldReturn400WhenDateFormatIsMalformed() throws Exception {
        mockMvc.perform(get("/api/v1/consumption-histories")
                        .param("from", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exportConsumptionHistoryCsvShouldReturnCsvAttachment() throws Exception {
        ConsumptionHistory row = new ConsumptionHistory(
                7L, 5L, "Compressor, Bay A", "Machinery", 2.4, 4.8, 3.36, "High Peak",
                LocalDateTime.of(2026, 11, 18, 9, 15)
        );
        when(queryService.handle(any(GetConsumptionHistoryQuery.class))).thenReturn(List.of(row));

        String body = mockMvc.perform(get("/api/v1/consumption-histories/export")
                        .param("userId", "1")
                        .param("from", "2026-11-01")
                        .param("to", "2026-11-30"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consumption-report.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(body.startsWith("id,deviceId,deviceName,deviceCategory,powerKw,energyKwh,cost,status,measuredAt"));
        assertTrue(body.contains("\"Compressor, Bay A\""));
        assertTrue(body.contains("High Peak"));
    }
}
