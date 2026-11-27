package com.jouletracker.platform.alerts.application.internal.commandservices;

import com.jouletracker.platform.alerts.application.commandservices.MonthlyGoalCommandService;
import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.commands.CreateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.DeleteMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.model.commands.UpdateMonthlyGoalCommand;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyGoalRepository;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Application service implementation for monthly consumption goal CRUD commands (US-28).
 */
@Service
@Transactional
public class MonthlyGoalCommandServiceImpl implements MonthlyGoalCommandService {

    private final MonthlyGoalRepository monthlyGoalRepository;

    public MonthlyGoalCommandServiceImpl(MonthlyGoalRepository monthlyGoalRepository) {
        this.monthlyGoalRepository = monthlyGoalRepository;
    }

    @Override
    public Result<MonthlyGoal, ApplicationError> handle(CreateMonthlyGoalCommand command) {
        if (command == null || command.userId() == null) {
            return Result.failure(ApplicationError.validationError("userId", "User identifier must be provided"));
        }
        if (command.targetKwh() == null || command.targetKwh() <= 0) {
            return Result.failure(ApplicationError.validationError("targetKwh", "Monthly target kWh must be greater than zero"));
        }

        LocalDate now = LocalDate.now();
        int resolvedYear = command.year() != null ? command.year() : now.getYear();
        int resolvedMonth = command.month() != null ? command.month() : now.getMonthValue();

        if (resolvedMonth < 1 || resolvedMonth > 12) {
            return Result.failure(ApplicationError.validationError("month", "Month must be between 1 and 12"));
        }

        if (monthlyGoalRepository.existsByUserIdAndYearAndMonth(command.userId(), resolvedYear, resolvedMonth)) {
            return Result.failure(ApplicationError.conflict(
                    "MONTHLY_GOAL",
                    "A monthly consumption goal already exists for " + resolvedYear + "-" + resolvedMonth
            ));
        }

        MonthlyGoal goal = new MonthlyGoal(null, command.userId(), command.targetKwh(), resolvedYear, resolvedMonth);
        return Result.success(monthlyGoalRepository.save(goal));
    }

    @Override
    public Result<MonthlyGoal, ApplicationError> handle(UpdateMonthlyGoalCommand command) {
        if (command == null || command.id() == null) {
            return Result.failure(ApplicationError.validationError("id", "Monthly goal identifier must be provided"));
        }
        if (command.targetKwh() != null && command.targetKwh() <= 0) {
            return Result.failure(ApplicationError.validationError("targetKwh", "Monthly target kWh must be greater than zero"));
        }
        if (command.month() != null && (command.month() < 1 || command.month() > 12)) {
            return Result.failure(ApplicationError.validationError("month", "Month must be between 1 and 12"));
        }

        var existingOpt = monthlyGoalRepository.findById(command.id());
        if (existingOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MONTHLY_GOAL", String.valueOf(command.id())));
        }

        MonthlyGoal goal = existingOpt.get();
        goal.updateGoal(command.targetKwh(), command.year(), command.month());
        return Result.success(monthlyGoalRepository.save(goal));
    }

    @Override
    public Result<Void, ApplicationError> handle(DeleteMonthlyGoalCommand command) {
        if (command == null || command.id() == null) {
            return Result.failure(ApplicationError.validationError("id", "Monthly goal identifier must be provided"));
        }
        var existingOpt = monthlyGoalRepository.findById(command.id());
        if (existingOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MONTHLY_GOAL", String.valueOf(command.id())));
        }

        monthlyGoalRepository.deleteById(command.id());
        return Result.success(null);
    }
}