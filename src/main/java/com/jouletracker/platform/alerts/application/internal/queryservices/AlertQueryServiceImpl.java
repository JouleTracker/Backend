package com.jouletracker.platform.alerts.application.internal.queryservices;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.application.queryservices.AlertQueryService;
import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.entities.AlertSummary;
import com.jouletracker.platform.alerts.domain.model.queries.GetActiveAlertsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertSummaryByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetAlertsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application query service implementation for retrieving alerts and KPI counts.
 */
@Service
@Transactional
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;
    private final AlertCommandService alertCommandService;

    public AlertQueryServiceImpl(AlertRepository alertRepository,
                                 AlertCommandService alertCommandService) {
        this.alertRepository = alertRepository;
        this.alertCommandService = alertCommandService;
    }

    @Override
    public List<Alert> handle(GetAlertsByUserIdQuery query) {
        Long resolvedUserId = (query != null && query.userId() != null) ? query.userId() : 1L;
        alertCommandService.evaluateUserSensors(resolvedUserId);
        return alertRepository.findAllByUserIdOrderByCreatedAtDesc(resolvedUserId);
    }

    @Override
    public List<Alert> handle(GetActiveAlertsByUserIdQuery query) {
        Long resolvedUserId = (query != null && query.userId() != null) ? query.userId() : 1L;
        alertCommandService.evaluateUserSensors(resolvedUserId);
        AlertSeverity filterSeverity = (query != null && query.severity() != null && !query.severity().isBlank())
                ? AlertSeverity.fromString(query.severity())
                : null;

        return alertRepository.findAllByUserIdOrderByCreatedAtDesc(resolvedUserId).stream()
                .filter(alert -> !alert.isRead())
                .filter(alert -> filterSeverity == null || alert.getSeverity() == filterSeverity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertSummary handle(GetAlertSummaryByUserIdQuery query) {
        Long resolvedUserId = (query != null && query.userId() != null) ? query.userId() : 1L;
        List<Alert> alerts = alertRepository.findAllByUserIdOrderByCreatedAtDesc(resolvedUserId);
        long totalCount = alerts.size();
        long resolvedCount = alerts.stream().filter(Alert::isRead).count();
        long activeCount = totalCount - resolvedCount;
        return new AlertSummary(resolvedUserId, totalCount, activeCount, resolvedCount);
    }
}