package com.jouletracker.platform.businessenergy.application.commandservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

/**
 * Application command service for business area management.
 */
public interface BusinessAreaCommandService {

    Result<BusinessArea, ApplicationError> handle(CreateBusinessAreaCommand command);
}
