package com.jouletracker.platform.devices.application.commandservices;

import com.jouletracker.platform.devices.domain.model.commands.SeedApplianceProfilesCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

public interface ApplianceProfileCommandService {
    Result<Void, ApplicationError> handle(SeedApplianceProfilesCommand command);
}
