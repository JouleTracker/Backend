package com.jouletracker.platform.devices.application.commandservices;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.commands.CreateDeviceCommand;
import com.jouletracker.platform.devices.domain.model.commands.DeleteDeviceCommand;
import com.jouletracker.platform.devices.domain.model.commands.UpdateDeviceCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

public interface DeviceCommandService {
    Result<Device, ApplicationError> handle(CreateDeviceCommand command);
    Result<Device, ApplicationError> handle(UpdateDeviceCommand command);
    Result<Void, ApplicationError> handle(DeleteDeviceCommand command);
}
