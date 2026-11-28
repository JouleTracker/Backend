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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumptionHistoryQueryServiceImplTest {

    @Mock
    private EnergyMeasurementRepository measurementRepository;

    @InjectMocks
    private ConsumptionHistoryQueryServiceImpl queryService;

    @Test
    void handleShouldFilterByExplicitDateRangeAndReturnEnglishStatusLabels() {
        LocalDateTime from = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 30, 23, 59, 59);

        EnergyMeasurement off = new EnergyMeasurement(1L, 1L, 10L, 5L, "Router", "IT", 0.0, 0.0, 220.0, 0.0, 0.0, LocalDateTime.of(2026, 11, 5, 8, 0));
        EnergyMeasurement standby = new EnergyMeasurement(2L, 1L, 10L, 5L, "TV", "Home", 0.03, 0.1, 220.0, 0.1, 0.07, LocalDateTime.of(2026, 11, 6, 9, 0));
        EnergyMeasurement normal = new EnergyMeasurement(3L, 1L, 10L, 5L, "Fridge", "Kitchen", 0.65, 1.2, 220.0, 2.9, 0.84, LocalDateTime.of(2026, 11, 7, 10, 0));
        EnergyMeasurement peak = new EnergyMeasurement(4L, 1L, 10L, 5L, "Compressor", "Industrial", 2.8, 3.5, 220.0, 12.7, 2.45, LocalDateTime.of(2026, 11, 8, 11, 0));

        when(measurementRepository.findAllByUserIdAndMeasuredAtBetween(eq(1L), eq(from), eq(to)))
                .thenReturn(List.of(off, standby, normal, peak));

        List<ConsumptionHistory> results = queryService.handle(
                new GetConsumptionHistoryQuery(1L, ConsumptionPeriod.MONTHLY, null, from, to)
        );

        assertEquals(4, results.size());
        assertEquals("High Peak", results.get(0).status());
        assertEquals("Normal", results.get(1).status());
        assertEquals("Standby", results.get(2).status());
        assertEquals("Off", results.get(3).status());
    }
}
