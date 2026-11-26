package com.jouletracker.platform.alerts.application.commandservices;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.commands.EvaluateConsumptionAlertCommand;
import com.jouletracker.platform.alerts.domain.model.commands.MarkAlertAsReadCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

import java.util.List;

/**
 * Application command service for consumption threshold evaluation and alert management (TS-14).
 */
public interface AlertCommandService {
    List<Alert> handle(EvaluateConsumptionAlertCommand command);
    List<Alert> evaluateUserSensors(Long userId);
    Result<Alert, ApplicationError> handle(MarkAlertAsReadCommand command);
}
