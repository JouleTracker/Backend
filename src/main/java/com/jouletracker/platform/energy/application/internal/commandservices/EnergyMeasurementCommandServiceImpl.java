package com.jouletracker.platform.energy.application.internal.commandservices;

import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.application.commandservices.EnergyMeasurementCommandService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class EnergyMeasurementCommandServiceImpl implements EnergyMeasurementCommandService {

    private final EnergyMeasurementRepository measurementRepository;
    private final DevicesContextFacade devicesContextFacade;

    public EnergyMeasurementCommandServiceImpl(EnergyMeasurementRepository measurementRepository,
                                               DevicesContextFacade devicesContextFacade) {
        this.measurementRepository = measurementRepository;
        this.devicesContextFacade = devicesContextFacade;
    }

    @Override
    @Transactional
    public Result<EnergyMeasurement, ApplicationError> handle(RegisterEnergyMeasurementCommand command) {
        if (command.userId() == null) {
            return Result.failure(ApplicationError.validationError("userId", "User ID is required"));
        }
        if (command.powerKw() == null || command.powerKw() < 0) {
            return Result.failure(ApplicationError.validationError("powerKw", "Power (kW) must be non-negative"));
        }
        if (command.energyKwh() == null || command.energyKwh() < 0) {
            return Result.failure(ApplicationError.validationError("energyKwh", "Energy (kWh) must be non-negative"));
        }

        if (command.sensorId() != null && !devicesContextFacade.existsSensorById(command.sensorId())) {
            return Result.failure(ApplicationError.notFound("Sensor", command.sensorId().toString()));
        }

        Long resolvedDeviceId = command.deviceId();
        if (resolvedDeviceId == null && command.sensorId() != null) {
            resolvedDeviceId = devicesContextFacade.fetchAssignedDeviceIdBySensorId(command.sensorId()).orElse(null);
        }

        if (resolvedDeviceId != null && !devicesContextFacade.existsDeviceById(resolvedDeviceId)) {
            return Result.failure(ApplicationError.notFound("Device", resolvedDeviceId.toString()));
        }

        var enrichedCommand = new RegisterEnergyMeasurementCommand(
                command.userId(),
                command.sensorId(),
                resolvedDeviceId,
                command.deviceName(),
                command.deviceCategory(),
                command.powerKw(),
                command.energyKwh(),
                command.voltage(),
                command.currentAmps(),
                command.measuredAt()
        );

        var measurement = new EnergyMeasurement(enrichedCommand);
        var saved = measurementRepository.save(measurement);

        if (saved.getSensorId() != null) {
            String timestamp = saved.getMeasuredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            devicesContextFacade.updateSensorTelemetry(
                    saved.getSensorId(),
                    saved.getPowerKw(),
                    saved.getEnergyKwh(),
                    timestamp
            );
        }

        return Result.success(saved);
    }
}