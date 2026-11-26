package com.jouletracker.platform.energy.application.internal.commandservices;

import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergyMeasurementCommandServiceImplTest {

    @Mock
    private EnergyMeasurementRepository measurementRepository;

    @Mock
    private DevicesContextFacade devicesContextFacade;

    @InjectMocks
    private EnergyMeasurementCommandServiceImpl commandService;

    @Test
    void handleShouldResolveDeviceSaveMeasurementAndUpdateSensorTelemetry() {
        LocalDateTime measuredAt = LocalDateTime.of(2026, 11, 13, 14, 30, 0);
        RegisterEnergyMeasurementCommand command = new RegisterEnergyMeasurementCommand(
                1L, 10L, null, "Refrigeradora", "Electrodomésticos",
                1.25, 0.42, 220.0, 5.68, measuredAt
        );

        when(devicesContextFacade.existsSensorById(10L)).thenReturn(true);
        when(devicesContextFacade.fetchAssignedDeviceIdBySensorId(10L)).thenReturn(Optional.of(5L));
        when(devicesContextFacade.existsDeviceById(5L)).thenReturn(true);
        when(measurementRepository.save(any(EnergyMeasurement.class))).thenAnswer(inv -> {
            EnergyMeasurement m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        Result<EnergyMeasurement, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isSuccess());
        EnergyMeasurement saved = ((Result.Success<EnergyMeasurement, ApplicationError>) result).value();
        assertEquals(99L, saved.getId());
        assertEquals(5L, saved.getDeviceId());
        assertEquals(0.29, saved.getCost());
        verify(devicesContextFacade).updateSensorTelemetry(eq(10L), eq(1.25), eq(0.42), anyString());
    }

    @Test
    void handleShouldReturnValidationErrorWhenUserIdIsNull() {
        RegisterEnergyMeasurementCommand command = new RegisterEnergyMeasurementCommand(
                null, 10L, 5L, "TV", "Entretenimiento",
                0.5, 0.2, 220.0, 2.27, LocalDateTime.now()
        );

        Result<EnergyMeasurement, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        verifyNoInteractions(measurementRepository);
    }

    @Test
    void handleShouldReturnValidationErrorWhenPowerKwIsNegative() {
        RegisterEnergyMeasurementCommand command = new RegisterEnergyMeasurementCommand(
                1L, 10L, 5L, "TV", "Entretenimiento",
                -0.5, 0.2, 220.0, 2.27, LocalDateTime.now()
        );

        Result<EnergyMeasurement, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("VALIDATION_ERROR", error.code());
        verifyNoInteractions(measurementRepository);
    }

    @Test
    void handleShouldReturnNotFoundWhenSensorDoesNotExist() {
        RegisterEnergyMeasurementCommand command = new RegisterEnergyMeasurementCommand(
                1L, 999L, null, "TV", "Entretenimiento",
                0.5, 0.2, 220.0, 2.27, LocalDateTime.now()
        );
        when(devicesContextFacade.existsSensorById(999L)).thenReturn(false);

        Result<EnergyMeasurement, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("SENSOR_NOT_FOUND", error.code());
        verifyNoInteractions(measurementRepository);
    }

    @Test
    void handleShouldReturnNotFoundWhenDeviceDoesNotExist() {
        RegisterEnergyMeasurementCommand command = new RegisterEnergyMeasurementCommand(
                1L, null, 888L, "TV", "Entretenimiento",
                0.5, 0.2, 220.0, 2.27, LocalDateTime.now()
        );
        when(devicesContextFacade.existsDeviceById(888L)).thenReturn(false);

        Result<EnergyMeasurement, ApplicationError> result = commandService.handle(command);

        assertTrue(result.isFailure());
        ApplicationError error = ((Result.Failure<EnergyMeasurement, ApplicationError>) result).error();
        assertEquals("DEVICE_NOT_FOUND", error.code());
        verifyNoInteractions(measurementRepository);
    }
}
