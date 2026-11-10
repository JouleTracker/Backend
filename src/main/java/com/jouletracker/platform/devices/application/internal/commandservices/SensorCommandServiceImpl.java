package com.jouletracker.platform.devices.application.internal.commandservices;

import com.jouletracker.platform.devices.application.commandservices.SensorCommandService;
import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.commands.DeleteSensorCommand;
import com.jouletracker.platform.devices.domain.model.commands.RegisterSensorCommand;
import com.jouletracker.platform.devices.domain.model.commands.UpdateSensorCommand;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;
import com.jouletracker.platform.devices.domain.repositories.ApplianceProfileRepository;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorCommandServiceImpl implements SensorCommandService {

    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final ApplianceProfileRepository applianceProfileRepository;

    public SensorCommandServiceImpl(SensorRepository sensorRepository,
                                    DeviceRepository deviceRepository,
                                    ApplianceProfileRepository applianceProfileRepository) {
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.applianceProfileRepository = applianceProfileRepository;
    }

    @Override
    public Result<Sensor, ApplicationError> handle(RegisterSensorCommand command) {
        if (command.serialNumber() == null || command.serialNumber().isBlank()) {
            return Result.failure(ApplicationError.validationError("serialNumber", "Serial number must not be blank"));
        }
        if (command.userId() == null) {
            return Result.failure(ApplicationError.validationError("userId", "User identifier must be provided"));
        }

        SerialNumber serialNumber;
        try {
            serialNumber = new SerialNumber(command.serialNumber());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("serialNumber", ex.getMessage()));
        }

        if (sensorRepository.existsBySerialNumber(serialNumber)) {
            return Result.failure(ApplicationError.conflict("SENSOR", "Sensor with this serial number is already registered"));
        }

        Sensor sensor = new Sensor(command);

        // Associate device if specified
        if (command.assignedDeviceId() != null) {
            var deviceOpt = deviceRepository.findById(command.assignedDeviceId());
            deviceOpt.ifPresent(device -> sensor.assignDevice(device.getId(), device.getName(), device.getCategory()));
        }

        // Associate reference appliance profile if specified
        if (command.applianceProfileId() != null) {
            var profileOpt = applianceProfileRepository.findById(command.applianceProfileId());
            profileOpt.ifPresent(profile -> sensor.assignApplianceProfile(profile.getId(), profile.getName(), profile.getRecommendedDailyKwh()));
        }

        Sensor savedSensor = sensorRepository.save(sensor);
        return Result.success(savedSensor);
    }

    @Override
    public Result<Sensor, ApplicationError> handle(UpdateSensorCommand command) {
        var sensorOpt = sensorRepository.findById(command.id());
        if (sensorOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR", String.valueOf(command.id())));
        }

        Sensor sensor = sensorOpt.get();
        sensor.updateConfiguration(command.name(), command.model(), command.status());

        // Update assigned device
        if (command.assignedDeviceId() != null) {
            var deviceOpt = deviceRepository.findById(command.assignedDeviceId());
            if (deviceOpt.isPresent()) {
                var device = deviceOpt.get();
                sensor.assignDevice(device.getId(), device.getName(), device.getCategory());
            } else {
                sensor.unassignDevice();
            }
        } else {
            sensor.unassignDevice();
        }

        // Update appliance profile
        if (command.applianceProfileId() != null) {
            var profileOpt = applianceProfileRepository.findById(command.applianceProfileId());
            if (profileOpt.isPresent()) {
                var profile = profileOpt.get();
                sensor.assignApplianceProfile(profile.getId(), profile.getName(), profile.getRecommendedDailyKwh());
            } else {
                sensor.unassignApplianceProfile();
            }
        } else {
            sensor.unassignApplianceProfile();
        }

        Sensor updatedSensor = sensorRepository.save(sensor);
        return Result.success(updatedSensor);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteSensorCommand command) {
        var sensorOpt = sensorRepository.findById(command.id());
        if (sensorOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SENSOR", String.valueOf(command.id())));
        }

        sensorRepository.deleteById(command.id());
        return Result.success(null);
    }
}
