package com.jouletracker.platform.devices.application.commandservices;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.commands.DeleteSensorCommand;
import com.jouletracker.platform.devices.domain.model.commands.RegisterSensorCommand;
import com.jouletracker.platform.devices.domain.model.commands.UpdateSensorCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

public interface SensorCommandService {
    Result<Sensor, ApplicationError> handle(RegisterSensorCommand command);
    Result<Sensor, ApplicationError> handle(UpdateSensorCommand command);
    Result<Void, ApplicationError> handle(DeleteSensorCommand command);
}
