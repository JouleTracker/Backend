package com.jouletracker.platform.alerts.application.commandservices;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.DeleteMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.UpdateMonthlyGoalCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

/**
 * Application command service for monthly energy consumption goals (US-28).
 */
public interface MonthlyGoalCommandService {
    Result<MonthlyGoal, ApplicationError> handle(CreateMonthlyGoalCommand command);
    Result<MonthlyGoal, ApplicationError> handle(UpdateMonthlyGoalCommand command);
    Result<Void, ApplicationError> handle(DeleteMonthlyGoalCommand command);
}