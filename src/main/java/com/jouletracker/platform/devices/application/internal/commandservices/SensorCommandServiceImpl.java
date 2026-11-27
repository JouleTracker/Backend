
package com.jouletracker.platform.devices.application.internal.commandservices;

import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
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

import java.util.Objects;

@Service
@Transactional
public class SensorCommandServiceImpl implements SensorCommandService {

    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final ApplianceProfileRepository applianceProfileRepository;
    private final BusinessAreaRepository businessAreaRepository;

    public SensorCommandServiceImpl(
            SensorRepository sensorRepository,
            DeviceRepository deviceRepository,
            ApplianceProfileRepository applianceProfileRepository,
            BusinessAreaRepository businessAreaRepository) {

        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.applianceProfileRepository = applianceProfileRepository;
        this.businessAreaRepository = businessAreaRepository;
    }

    @Override
    public Result<Sensor, ApplicationError> handle(RegisterSensorCommand command) {

        if (command.serialNumber() == null || command.serialNumber().isBlank()) {
            return Result.failure(
                    ApplicationError.validationError(
                            "serialNumber", "Serial number must not be blank"
                    )
            );
        }

        if (command.userId() == null) {
            return Result.failure(
                    ApplicationError.validationError(
                            "userId", "User identifier must be provided"
                    )
            );
        }

        SerialNumber serialNumber;

        try {
            serialNumber = new SerialNumber(command.serialNumber());
        } catch (IllegalArgumentException ex) {
            return Result.failure(
                    ApplicationError.validationError(
                            "serialNumber", ex.getMessage()
                    )
            );
        }

        if (sensorRepository.existsBySerialNumber(serialNumber)) {
            return Result.failure(
                    ApplicationError.conflict(
                            "SENSOR",
                            "Sensor with this serial number is already registered"
                    )
            );
        }

        // US-32: Validate business area ownership.
        ApplicationError areaError = validateBusinessArea(
                command.businessAreaId(),
                command.userId()
        );

        if (areaError != null) {
            return Result.failure(areaError);
        }

        Sensor sensor = new Sensor(command);

        // US-32: Associate business area if specified.
        if (command.businessAreaId() != null) {
            sensor.assignBusinessArea(command.businessAreaId());
        }

        // Associate device if specified.
        if (command.assignedDeviceId() != null) {
            var deviceOpt = deviceRepository.findById(
                    command.assignedDeviceId()
            );

            deviceOpt.ifPresent(device ->
                    sensor.assignDevice(
                            device.getId(),
                            device.getName(),
                            device.getCategory()
                    )
            );
        }

        // Associate reference appliance profile if specified.
        if (command.applianceProfileId() != null) {
            var profileOpt = applianceProfileRepository.findById(
                    command.applianceProfileId()
            );

            profileOpt.ifPresent(profile ->
                    sensor.assignApplianceProfile(
                            profile.getId(),
                            profile.getName(),
                            profile.getRecommendedDailyKwh()
                    )
            );
        }

        Sensor savedSensor = sensorRepository.save(sensor);

        return Result.success(savedSensor);
    }

    @Override
    public Result<Sensor, ApplicationError> handle(UpdateSensorCommand command) {

        var sensorOpt = sensorRepository.findById(command.id());

        if (sensorOpt.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound(
                            "SENSOR", String.valueOf(command.id())
                    )
            );
        }

        Sensor sensor = sensorOpt.get();

        // US-32: Prevent modification of another user's sensor.
        if (!Objects.equals(sensor.getUserId(), command.userId())) {
            return Result.failure(
                    ApplicationError.forbidden(
                            "The sensor does not belong to the current user"
                    )
            );
        }

        // US-32: Validate business area before updating the sensor.
        ApplicationError areaError = validateBusinessArea(
                command.businessAreaId(),
                sensor.getUserId()
        );

        if (areaError != null) {
            return Result.failure(areaError);
        }

        sensor.updateConfiguration(
                command.name(),
                command.model(),
                command.status()
        );

        // US-32: Update or remove business area association.
        if (command.businessAreaId() != null) {
            sensor.assignBusinessArea(command.businessAreaId());
        } else {
            sensor.unassignBusinessArea();
        }

        // Update assigned device.
        if (command.assignedDeviceId() != null) {
            var deviceOpt = deviceRepository.findById(
                    command.assignedDeviceId()
            );

            if (deviceOpt.isPresent()) {
                var device = deviceOpt.get();

                sensor.assignDevice(
                        device.getId(),
                        device.getName(),
                        device.getCategory()
                );
            } else {
                sensor.unassignDevice();
            }
        } else {
            sensor.unassignDevice();
        }

        // Update appliance profile.
        if (command.applianceProfileId() != null) {
            var profileOpt = applianceProfileRepository.findById(
                    command.applianceProfileId()
            );

            if (profileOpt.isPresent()) {
                var profile = profileOpt.get();

                sensor.assignApplianceProfile(
                        profile.getId(),
                        profile.getName(),
                        profile.getRecommendedDailyKwh()
                );
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
            return Result.failure(
                    ApplicationError.notFound(
                            "SENSOR", String.valueOf(command.id())
                    )
            );
        }

        sensorRepository.deleteById(command.id());

        return Result.success(null);
    }

    /**
     * US-32: Validates that the business area exists and belongs
     * to the sensor owner.
     */
    private ApplicationError validateBusinessArea(
            Long businessAreaId,
            Long userId) {

        if (businessAreaId == null) {
            return null;
        }

        if (businessAreaId <= 0) {
            return ApplicationError.validationError(
                    "businessAreaId",
                    "Business area identifier must be greater than zero"
            );
        }

        var businessAreaOpt = businessAreaRepository.findById(businessAreaId);

        if (businessAreaOpt.isEmpty()) {
            return ApplicationError.notFound(
                    "BUSINESS_AREA",
                    String.valueOf(businessAreaId)
            );
        }

        if (!Objects.equals(businessAreaOpt.get().getUserId(), userId)) {
            return ApplicationError.forbidden(
                    "The business area does not belong to the current user"
            );
        }

        return null;
    }
}
