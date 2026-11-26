package com.jouletracker.platform.energy.application.commandservices;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

public interface EnergyMeasurementCommandService {
    Result<EnergyMeasurement, ApplicationError> handle(RegisterEnergyMeasurementCommand command);
}