package com.jouletracker.platform.devices.application.internal.commandservices;

import com.jouletracker.platform.devices.application.commandservices.DeviceCommandService;
import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.commands.CreateDeviceCommand;
import com.jouletracker.platform.devices.domain.model.commands.DeleteDeviceCommand;
import com.jouletracker.platform.devices.domain.model.commands.UpdateDeviceCommand;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceRepository deviceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DeviceCommandServiceImpl(DeviceRepository deviceRepository, ApplicationEventPublisher eventPublisher) {
        this.deviceRepository = deviceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Result<Device, ApplicationError> handle(CreateDeviceCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            return Result.failure(ApplicationError.validationError("name", "Device name must not be blank"));
        }
        if (command.userId() == null) {
            return Result.failure(ApplicationError.validationError("userId", "User identifier must be provided"));
        }
        if (deviceRepository.existsByUserIdAndName(command.userId(), command.name().trim())) {
            return Result.failure(ApplicationError.conflict("DEVICE", "A device with this name already exists in your inventory"));
        }

        Device device = new Device(command);
        Device savedDevice = deviceRepository.save(device);
        return Result.success(savedDevice);
    }

    @Override
    public Result<Device, ApplicationError> handle(UpdateDeviceCommand command) {
        var deviceOptional = deviceRepository.findById(command.id());
        if (deviceOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("DEVICE", String.valueOf(command.id())));
        }

        Device device = deviceOptional.get();
        device.updateDetails(
                command.name(),
                command.location(),
                command.category(),
                command.status(),
                command.powerLimitKw(),
                command.maintenanceDueDate()
        );

        Device updatedDevice = deviceRepository.save(device);
        return Result.success(updatedDevice);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteDeviceCommand command) {
        var deviceOptional = deviceRepository.findById(command.id());
        if (deviceOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("DEVICE", String.valueOf(command.id())));
        }

        Device device = deviceOptional.get();
        device.onDeleted();
        device.domainEvents().forEach(eventPublisher::publishEvent);
        device.clearDomainEvents();

        deviceRepository.deleteById(command.id());
        return Result.success(null);
    }
}
