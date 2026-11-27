package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.ConsumptionHistory;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumptionHistoryQueryServiceImplTest {

    @Mock
    private EnergyMeasurementRepository measurementRepository;

    @InjectMocks
    private ConsumptionHistoryQueryServiceImpl queryService;

    @Test
    void handleShouldClassifyAllStatusLevelsInEnglishAndSortNewestFirst() {
        LocalDateTime now = LocalDateTime.now();
        List<EnergyMeasurement> measurements = List.of(
                new EnergyMeasurement(1L, 1L, 10L, 5L, "TV", "Entertainment", 0.0, 0.0, 220.0, 0.0, 0.0, now.minusMinutes(40)),
                new EnergyMeasurement(2L, 1L, 10L, 5L, "TV", "Entertainment", 0.02, 0.01, 220.0, 0.09, 0.01, now.minusMinutes(30)),
                new EnergyMeasurement(3L, 1L, 10L, 5L, "Fridge", "Appliances", 0.45, 0.30, 220.0, 2.05, 0.21, now.minusMinutes(20)),
                new EnergyMeasurement(4L, 1L, 10L, 5L, "AC", "Climate", 2.50, 1.20, 220.0, 11.36, 0.84, now.minusMinutes(10))
        );

        when(measurementRepository.findAllByUserIdAndMeasuredAtBetween(eq(1L), any(), any()))
                .thenReturn(measurements);

        List<ConsumptionHistory> result = queryService.handle(
                new GetConsumptionHistoryQuery(1L, ConsumptionPeriod.MONTHLY, null)
        );

        assertEquals(4, result.size());
        assertEquals("High Peak", result.get(0).status());
        assertEquals("Normal", result.get(1).status());
        assertEquals("Standby", result.get(2).status());
        assertEquals("Off", result.get(3).status());
    }

    @Test
    void handleShouldFilterByDeviceIdWhenProvided() {
        LocalDateTime now = LocalDateTime.now();
        EnergyMeasurement measurement = new EnergyMeasurement(
                9L, 1L, 10L, 5L, "Heater", "Climate", 1.5, 0.75, 220.0, 6.82, 0.53, now
        );
        when(measurementRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetween(eq(1L), eq(5L), any(), any()))
                .thenReturn(List.of(measurement));

        List<ConsumptionHistory> result = queryService.handle(
                new GetConsumptionHistoryQuery(1L, ConsumptionPeriod.DAILY, 5L)
        );

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).deviceId());
        assertEquals("Normal", result.get(0).status());
    }
}