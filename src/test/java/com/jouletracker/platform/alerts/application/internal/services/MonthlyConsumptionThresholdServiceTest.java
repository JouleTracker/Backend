
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyConsumptionThresholdRepository;
import com.jouletracker.platform.energy.interfaces.acl.EnergyContextFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonthlyConsumptionThresholdServiceTest {

    private static final LocalDateTime REFERENCE =
            LocalDateTime.of(2026, 11, 27, 10, 0);

    @Mock
    private MonthlyConsumptionThresholdRepository thresholdRepository;

    @Mock
    private EnergyContextFacade energyContextFacade;

    @InjectMocks
    private MonthlyConsumptionThresholdService thresholdService;

    @Test
    void shouldReturnEmptyWhenThresholdIsNotConfigured() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertTrue(
                thresholdService.getThreshold(1L).isEmpty()
        );

        verifyNoInteractions(energyContextFacade);
    }

    @Test
    void shouldCreateMonthlyThreshold() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(thresholdRepository.save(
                any(MonthlyConsumptionThreshold.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        var result = thresholdService.updateThreshold(
                1L,
                new BigDecimal("150")
        );

        assertEquals(1L, result.getUserId());

        assertEquals(
                new BigDecimal("150.000"),
                result.getMonthlyLimitKwh()
        );

        verify(thresholdRepository).save(result);
    }

    @Test
    void shouldUpdateExistingThreshold() {

        var existing = new MonthlyConsumptionThreshold(
                1L,
                new BigDecimal("150")
        );

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.of(existing));

        when(thresholdRepository.save(existing))
                .thenReturn(existing);

        var result = thresholdService.updateThreshold(
                1L,
                new BigDecimal("200")
        );

        assertEquals(
                new BigDecimal("200.000"),
                result.getMonthlyLimitKwh()
        );

        verify(thresholdRepository).save(existing);
    }

    @Test
    void shouldCalculateConsumptionBelowThreshold() {

        mockConfiguredThreshold("150", "120");

        var status = thresholdService.getConsumptionStatus(
                1L,
                REFERENCE
        );

        assertTrue(status.configured());

        assertFalse(status.thresholdReached());

        assertEquals("2026-11", status.period());

        assertEquals(
                new BigDecimal("30.000"),
                status.remainingKwh()
        );

        assertEquals(
                new BigDecimal("80.00"),
                status.consumptionPercentage()
        );
    }

    @Test
    void shouldDetectExactMonthlyThreshold() {

        mockConfiguredThreshold("150", "150");

        var status = thresholdService.getConsumptionStatus(
                1L,
                REFERENCE
        );

        assertTrue(status.thresholdReached());

        assertEquals(
                0,
                status.remainingKwh()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                new BigDecimal("100.00"),
                status.consumptionPercentage()
        );
    }

    @Test
    void shouldDetectExceededMonthlyThreshold() {

        mockConfiguredThreshold("150", "165");

        var status = thresholdService.getConsumptionStatus(
                1L,
                REFERENCE
        );

        assertTrue(status.thresholdReached());

        assertEquals(
                new BigDecimal("110.00"),
                status.consumptionPercentage()
        );

        assertEquals(
                0,
                status.remainingKwh()
                        .compareTo(BigDecimal.ZERO)
        );
    }

    @Test
    void shouldReturnConsumptionWithoutConfiguredThreshold() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        when(energyContextFacade.fetchMonthlyConsumptionKwh(
                1L,
                REFERENCE
        )).thenReturn(new BigDecimal("45"));

        var status = thresholdService.getConsumptionStatus(
                1L,
                REFERENCE
        );

        assertFalse(status.configured());

        assertFalse(status.thresholdReached());

        assertNull(status.monthlyLimitKwh());

        assertNull(status.remainingKwh());

        assertNull(status.consumptionPercentage());

        assertEquals(
                new BigDecimal("45"),
                status.currentConsumptionKwh()
        );
    }

    @Test
    void shouldRejectInvalidUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> thresholdService.getThreshold(0L)
        );

        verifyNoInteractions(
                thresholdRepository,
                energyContextFacade
        );
    }

    @Test
    void shouldRejectNegativeThreshold() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> thresholdService.updateThreshold(
                        1L,
                        new BigDecimal("-100")
                )
        );

        verify(thresholdRepository, never())
                .save(any(MonthlyConsumptionThreshold.class));
    }

    @Test
    void shouldRejectZeroThreshold() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> thresholdService.updateThreshold(
                        1L,
                        BigDecimal.ZERO
                )
        );

        verify(thresholdRepository, never())
                .save(any(MonthlyConsumptionThreshold.class));
    }

    @Test
    void shouldRejectExcessiveDecimalPrecision() {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> thresholdService.updateThreshold(
                        1L,
                        new BigDecimal("150.1234")
                )
        );

        verify(thresholdRepository, never())
                .save(any(MonthlyConsumptionThreshold.class));
    }

    private void mockConfiguredThreshold(
            String limit,
            String consumption) {

        when(thresholdRepository.findByUserId(1L))
                .thenReturn(Optional.of(
                        new MonthlyConsumptionThreshold(
                                1L,
                                new BigDecimal(limit)
                        )
                ));

        when(energyContextFacade.fetchMonthlyConsumptionKwh(
                1L,
                REFERENCE
        )).thenReturn(new BigDecimal(consumption));
    }
}
