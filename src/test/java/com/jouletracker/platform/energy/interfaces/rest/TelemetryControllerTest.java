package com.jouletracker.platform.energy.interfaces.rest;

import com.jouletracker.platform.energy.application.commandservices.EnergyMeasurementCommandService;
import com.jouletracker.platform.energy.application.queryservices.EnergyReadingQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TelemetryControllerTest {

    @Mock
    private EnergyMeasurementCommandService commandService;

    @Mock
    private EnergyReadingQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TelemetryController controller = new TelemetryController(commandService, queryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void receiveEnergyReadingShouldReturn201CreatedWithTelemetryResource() throws Exception {
        LocalDateTime measuredAt = LocalDateTime.of(2026, 11, 13, 15, 0, 0);
        EnergyMeasurement saved = new EnergyMeasurement(
                100L, 1L, 10L, 5L, "Refrigeradora", "Electrodomésticos",
                1.25, 0.42, 220.0, 5.68, 0.29, measuredAt
        );
        when(commandService.handle(any())).thenReturn(Result.success(saved));

        String payload = """
                {
                  "userId": 1,
                  "sensorId": 10,
                  "deviceId": 5,
                  "deviceName": "Refrigeradora",
                  "deviceCategory": "Electrodomésticos",
                  "powerKw": 1.25,
                  "energyKwh": 0.42,
                  "voltage": 220.0,
                  "currentAmps": 5.68
                }
                """;

        mockMvc.perform(post("/api/v1/telemetry/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.sensorId").value(10))
                .andExpect(jsonPath("$.deviceId").value(5))
                .andExpect(jsonPath("$.powerKw").value(1.25))
                .andExpect(jsonPath("$.energyKwh").value(0.42))
                .andExpect(jsonPath("$.voltage").value(220.0))
                .andExpect(jsonPath("$.currentAmps").value(5.68))
                .andExpect(jsonPath("$.cost").value(0.29));
    }

    @Test
    void receiveEnergyReadingShouldReturn404WhenSensorNotFound() throws Exception {
        when(commandService.handle(any())).thenReturn(Result.failure(ApplicationError.notFound("Sensor", "999")));

        String payload = """
                {
                  "userId": 1,
                  "sensorId": 999,
                  "powerKw": 1.0,
                  "energyKwh": 0.5
                }
                """;

        mockMvc.perform(post("/api/v1/telemetry/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getLatestSensorReadingShouldReturn200OkWithTelemetryResource() throws Exception {
        LocalDateTime measuredAt = LocalDateTime.of(2026, 11, 13, 15, 5, 0);
        EnergyMeasurement latest = new EnergyMeasurement(
                55L, 1L, 10L, 5L, "Refrigeradora", "Electrodomésticos",
                1.10, 0.35, 221.5, 4.97, 0.25, measuredAt
        );
        when(queryService.handle(any(com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery.class)))
                .thenReturn(Result.success(latest));

        mockMvc.perform(get("/api/v1/sensors/10/readings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(55))
                .andExpect(jsonPath("$.sensorId").value(10))
                .andExpect(jsonPath("$.voltage").value(221.5))
                .andExpect(jsonPath("$.currentAmps").value(4.97));
    }

    @Test
    void getLatestSensorReadingShouldReturn404WhenNoReadingsExist() throws Exception {
        when(queryService.handle(any(com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery.class)))
                .thenReturn(Result.failure(ApplicationError.notFound("EnergyMeasurement", "sensor:10")));

        mockMvc.perform(get("/api/v1/sensors/10/readings/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
