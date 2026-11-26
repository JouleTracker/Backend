package com.jouletracker.platform.alerts.application.queryservices;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.entities.AlertSummary;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertSummaryByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertsByUserIdQuery;

import java.util.List;

/**
 * Application query service for retrieving alerts and alert summaries.
 */
public interface AlertQueryService {
    List<Alert> handle(GetAlertsByUserIdQuery query);
    AlertSummary handle(GetAlertSummaryByUserIdQuery query);
}
