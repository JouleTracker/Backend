package com.jouletracker.platform.energy.domain.model.valueobjects;

import com.jouletracker.platform.energy.application.internal.queryservices.ComparativeConsumptionQueryServiceImpl;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.ComparativeConsumption;
import com.jouletracker.platform.energy.domain.model.queries.GetComparativeConsumptionQuery;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumptionPeriodTest {

    @Mock
    private EnergyMeasurementRepository measurementRepository;

    @Test
    void fromStringShouldSupportSpanishAndEnglishPeriodCodes() {
        assertEquals(ConsumptionPeriod.DAILY, ConsumptionPeriod.fromString("dia"));
        assertEquals(ConsumptionPeriod.DAILY, ConsumptionPeriod.fromString("Día"));
        assertEquals(ConsumptionPeriod.DAILY, ConsumptionPeriod.fromString("daily"));
        assertEquals(ConsumptionPeriod.WEEKLY, ConsumptionPeriod.fromString("semana"));
        assertEquals(ConsumptionPeriod.WEEKLY, ConsumptionPeriod.fromString("weekly"));
        assertEquals(ConsumptionPeriod.MONTHLY, ConsumptionPeriod.fromString("mes"));
        assertEquals(ConsumptionPeriod.MONTHLY, ConsumptionPeriod.fromString("MONTHLY"));
        assertEquals(ConsumptionPeriod.YEARLY, ConsumptionPeriod.fromString("ano"));
        assertEquals(ConsumptionPeriod.YEARLY, ConsumptionPeriod.fromString("año"));
        assertEquals(ConsumptionPeriod.YEARLY, ConsumptionPeriod.fromString("yearly"));
        assertEquals(ConsumptionPeriod.DAILY, ConsumptionPeriod.fromString(null));
        assertEquals(ConsumptionPeriod.DAILY, ConsumptionPeriod.fromString(" "));
    }

    @Test
    void monthlyComparativeShouldGroupByElapsedDaysFromPeriodStart() {
        ComparativeConsumptionQueryServiceImpl service = new ComparativeConsumptionQueryServiceImpl(measurementRepository);
        LocalDateTime now = LocalDateTime.now();

        EnergyMeasurement week1Measurement = new EnergyMeasurement(
                1L, 1L, 10L, 5L, "Refri", "Electrodomésticos",
                1.0, 2.5, 220.0, 4.5, 1.75, now.minusDays(28)
        );
        EnergyMeasurement week4Measurement = new EnergyMeasurement(
                2L, 1L, 10L, 5L, "Refri", "Electrodomésticos",
                1.0, 5.0, 220.0, 4.5, 3.50, now.minusDays(1)
        );
        EnergyMeasurement previousWeek2Measurement = new EnergyMeasurement(
                3L, 1L, 10L, 5L, "Refri", "Electrodomésticos",
                1.0, 4.0, 220.0, 4.5, 2.80, now.minusDays(50)
        );

        when(measurementRepository.findAllByUserIdAndMeasuredAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(week1Measurement, week4Measurement))
                .thenReturn(List.of(previousWeek2Measurement));

        List<ComparativeConsumption> buckets = service.handle(new GetComparativeConsumptionQuery(1L, ConsumptionPeriod.MONTHLY));

        assertEquals(4, buckets.size());
        assertEquals("Sem 1", buckets.get(0).label());
        assertEquals(2.5, buckets.get(0).currentPeriodKwh());
        assertEquals("Sem 2", buckets.get(1).label());
        assertEquals(4.0, buckets.get(1).previousPeriodKwh());
        assertEquals("Sem 4", buckets.get(3).label());
        assertEquals(5.0, buckets.get(3).currentPeriodKwh());
    }
}
