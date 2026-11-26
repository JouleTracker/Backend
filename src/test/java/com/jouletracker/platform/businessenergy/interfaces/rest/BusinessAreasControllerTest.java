package com.jouletracker.platform.businessenergy.interfaces.rest;

import com.jouletracker.platform.businessenergy.application.commandservices.BusinessAreaCommandService;
import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BusinessAreasControllerTest {

    @Mock
    private BusinessAreaCommandService commandService;

    @Mock
    private BusinessAreaQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BusinessAreasController controller = new BusinessAreasController(commandService, queryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getBusinessDashboardMetricsShouldReturn200WithAggregatedMetrics() throws Exception {
        BusinessAreaMetrics area = new BusinessAreaMetrics(
                1L, 1L, "Cocina", "Piso 1", "Cocina", "restaurant",
                2, 1, 1.5, 10.0, 7.0, true
        );
        BusinessDashboardMetrics dashboard = new BusinessDashboardMetrics(
                1L, "daily", 1, 1.5, 10.0, 7.0, "Cocina", List.of(area)
        );
        when(queryService.handle(any(GetBusinessDashboardMetricsQuery.class))).thenReturn(dashboard);

        mockMvc.perform(get("/api/v1/business-areas/metrics").param("userId", "1").param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalAreas").value(1))
                .andExpect(jsonPath("$.totalPowerKw").value(1.5))
                .andExpect(jsonPath("$.totalTodayKwh").value(10.0))
                .andExpect(jsonPath("$.highestConsumptionAreaName").value("Cocina"))
                .andExpect(jsonPath("$.areas[0].name").value("Cocina"))
                .andExpect(jsonPath("$.areas[0].hasDevices").value(true));
    }

    @Test
    void getBusinessAreaMetricsByIdShouldReturn404WhenAreaNotFound() throws Exception {
        when(queryService.handle(any(GetBusinessAreaMetricsByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/business-areas/999/metrics"))
                .andExpect(status().isNotFound());
    }
}
