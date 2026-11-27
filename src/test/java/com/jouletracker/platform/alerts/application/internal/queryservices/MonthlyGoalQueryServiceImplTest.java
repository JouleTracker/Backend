package com.jouletracker.platform.alerts.application.internal.queryservices;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.entities.MonthlyGoalProgress;
import com.jouletracker.platform.alerts.domain.model.queries.GetCurrentMonthlyGoalQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyGoalRepository;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyGoalQueryServiceImplTest {

    @Mock
    private MonthlyGoalRepository monthlyGoalRepository;

    @Mock
    private EnergyMeasurementRepository energyMeasurementRepository;

    @InjectMocks
    private MonthlyGoalQueryServiceImpl queryService;

    @Test
    void getCurrentGoalShouldComputeProgressAndRemainingKwh() {
        MonthlyGoal goal = new MonthlyGoal(1L, 1L, 200.0, 2026, 11);
        EnergyMeasurement m1 = new EnergyMeasurement(
                10L, 1L, 2L, 5L, "Fridge", "Appliances", 0.5, 80.0, 220.0, 2.27, 56.0,
                LocalDateTime.of(2026, 11, 10, 12, 0)
        );
        EnergyMeasurement m2 = new EnergyMeasurement(
                11L, 1L, 2L, 5L, "AC", "Climate", 1.2, 40.0, 220.0, 5.45, 28.0,
                LocalDateTime.of(2026, 11, 15, 14, 0)
        );

        when(monthlyGoalRepository.findByUserIdAndYearAndMonth(1L, 2026, 11)).thenReturn(Optional.of(goal));
        when(energyMeasurementRepository.findAllByUserIdAndMeasuredAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(m1, m2));

        Optional<MonthlyGoalProgress> result = queryService.handle(new GetCurrentMonthlyGoalQuery(1L, 2026, 11));

        assertTrue(result.isPresent());
        assertEquals(120.0, result.get().currentKwh());
        assertEquals(60.0, result.get().progressPercentage());
        assertEquals(80.0, result.get().remainingKwh());
        assertFalse(result.get().exceeded());
    }

    @Test
    void getMonthlyGoalsShouldFlagExceededWhenCurrentExceedsTarget() {
        MonthlyGoal goal = new MonthlyGoal(2L, 1L, 100.0, 2026, 11);
        EnergyMeasurement m1 = new EnergyMeasurement(
                12L, 1L, 2L, 5L, "Heater", "Climate", 2.0, 125.0, 220.0, 9.09, 87.5,
                LocalDateTime.of(2026, 11, 20, 18, 0)
        );

        when(monthlyGoalRepository.findAllByUserId(1L)).thenReturn(List.of(goal));
        when(energyMeasurementRepository.findAllByUserIdAndMeasuredAtBetween(eq(1L), any(), any()))
                .thenReturn(List.of(m1));

        List<MonthlyGoalProgress> results = queryService.handle(new GetMonthlyGoalsByUserIdQuery(1L));

        assertEquals(1, results.size());
        assertTrue(results.get(0).exceeded());
        assertEquals(0.0, results.get(0).remainingKwh());
        assertEquals(125.0, results.get(0).progressPercentage());
    }
}