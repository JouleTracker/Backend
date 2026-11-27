package com.jouletracker.platform.alerts.application.queryservices;

import com.jouletracker.platform.alerts.domain.model.entities.MonthlyGoalProgress;
import com.jouletracker.platform.alerts.domain.model.queries.GetCurrentMonthlyGoalQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalByIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalsByUserIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application query service for retrieving monthly consumption goals and progress (US-28).
 */
public interface MonthlyGoalQueryService {
    List<MonthlyGoalProgress> handle(GetMonthlyGoalsByUserIdQuery query);
    Optional<MonthlyGoalProgress> handle(GetMonthlyGoalByIdQuery query);
    Optional<MonthlyGoalProgress> handle(GetCurrentMonthlyGoalQuery query);
}