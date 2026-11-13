package com.jouletracker.platform.alerts.interfaces.rest.transform;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.entities.AlertSummary;
import com.jouletracker.platform.alerts.interfaces.rest.resources.AlertResource;
import com.jouletracker.platform.alerts.interfaces.rest.resources.AlertSummaryResource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Assembler converting Alert aggregates and AlertSummary read-models into REST resources.
 */
public final class AlertResourceAssembler {

    private AlertResourceAssembler() {
    }

    public static AlertResource toResourceFromEntity(Alert alert) {
        if (alert == null) {
            return null;
        }
        LocalDateTime createdAt = alert.getCreatedAt() != null ? alert.getCreatedAt() : LocalDateTime.now();
        return new AlertResource(
                alert.getId(),
                alert.getUserId(),
                alert.getSensorId(),
                alert.getDeviceId(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getDescription(),
                formatTimeAgo(createdAt),
                createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                alert.getSeverity() != null ? alert.getSeverity().getCode() : "warning",
                alert.getActionUrl(),
                alert.isRead(),
                alert.getStatus() != null ? alert.getStatus().getDisplayName() : "Activa"
        );
    }

    public static AlertSummaryResource toSummaryResourceFromEntity(AlertSummary summary) {
        if (summary == null) {
            return null;
        }
        return new AlertSummaryResource(
                summary.userId(),
                summary.totalCount(),
                summary.activeCount(),
                summary.resolvedCount()
        );
    }

    private static String formatTimeAgo(LocalDateTime createdAt) {
        long minutes = Math.max(0, Duration.between(createdAt, LocalDateTime.now()).toMinutes());
        if (minutes < 1) {
            return "Ahora";
        }
        if (minutes < 60) {
            return "Hace " + minutes + " min";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return "Hace " + hours + " h";
        }
        long days = hours / 24;
        return "Hace " + days + " d";
    }
}
