package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.EnergyReading;
import com.jouletracker.platform.energy.domain.model.queries.GetEnergyReadingsQuery;
import com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
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
class EnergyReadingQueryServiceImplTest {

    @Mock
    private EnergyMeasurementRepository measurementRepository;

    @Mock
    private DevicesContextFacade devicesContextFacade;

    @InjectMocks
    private EnergyReadingQueryServiceImpl queryService;

    @Test
    void handleLatestShouldReturnMeasurementWhenSensorAndReadingExist() {
        LocalDateTime measuredAt = LocalDateTime.of(2026, 11, 13, 15, 0, 0);
        EnergyMeasurement latest = new EnergyMeasurement(
                42L, 1L, 10L, 5L, "Refrigeradora", "Electrodomésticos",
                1.2, 0.4, 220.0, 5.45, 0.28, measuredAt
        );
        when(devicesContextFacade.existsSensorById(10L)).thenReturn(true);
        when(measurementRepository.findTopBySensorIdOrderByMeasuredAtDesc(10L)).thenReturn(Optional.of(latest));

        Result<EnergyMeasurement, ApplicationError> result = queryService.handle(new GetLatestReadingBySensorIdQuery(10L));

        assertTrue(result.isSuccess());
        EnergyMeasurement value = ((Result.Success<EnergyMeasurement, ApplicationError>) result).value();
        assertEquals(42L, value.getId());
        assertEquals(10L, value.getSensorId());
        assertEquals(220.0, value.getVoltage());
    }

    @Test
    void handleLatestShouldReturnValidationErrorWhenSensorIdIsNull() {
        Result<EnergyMeasurement, ApplicationError> result = queryService.handle(new GetLatestReadingBySensorIdQuery(null));

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
    }

    @Test
    void handleLatestShouldReturnSensorNotFoundWhenSensorDoesNotExist() {
        when(devicesContextFacade.existsSensorById(77L)).thenReturn(false);

        Result<EnergyMeasurement, ApplicationError> result = queryService.handle(new GetLatestReadingBySensorIdQuery(77L));

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("SENSOR_NOT_FOUND", error.code());
    }

    @Test
    void handleLatestShouldReturnMeasurementNotFoundWhenSensorHasNoReadings() {
        when(devicesContextFacade.existsSensorById(10L)).thenReturn(true);
        when(measurementRepository.findTopBySensorIdOrderByMeasuredAtDesc(10L)).thenReturn(Optional.empty());

        Result<EnergyMeasurement, ApplicationError> result = queryService.handle(new GetLatestReadingBySensorIdQuery(10L));

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("ENERGYMEASUREMENT_NOT_FOUND", error.code());
    }

    @Test
    void handleReadingsByDeviceShouldMapFormattedEnergyReadings() {
        LocalDateTime measuredAt = LocalDateTime.of(2026, 11, 13, 9, 15, 0);
        EnergyMeasurement m = new EnergyMeasurement(
                7L, 1L, 10L, 5L, "Laptop", "Cómputo",
                0.09, 0.123, 220.0, 0.41, 0.086, measuredAt
        );
        when(measurementRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetween(eq(1L), eq(5L), any(), any()))
                .thenReturn(List.of(m));

        List<EnergyReading> readings = queryService.handle(new GetEnergyReadingsQuery(1L, ConsumptionPeriod.DAILY, 5L));

        assertEquals(1, readings.size());
        assertEquals("09:15", readings.get(0).label());
        assertEquals(0.12, readings.get(0).kwh());
        assertEquals(0.09, readings.get(0).cost());
    }
}
