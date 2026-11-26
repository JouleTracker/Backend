package com.jouletracker.platform.alerts.domain.model.commands;

/**
 * Command to mark a consumption alert as read/resolved.
 */
public record MarkAlertAsReadCommand(Long alertId) {
}
