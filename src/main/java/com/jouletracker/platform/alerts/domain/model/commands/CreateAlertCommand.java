package com.jouletracker.platform.alerts.domain.model.commands;

import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;

/**
 * Command to create a targeted administrative alert or global broadcast alert (US-50).
 */
public record CreateAlertCommand(
        Long userId,
        boolean broadcast,
        String title,
        String description,
        AlertSeverity severity,
        String actionUrl
) {
}
