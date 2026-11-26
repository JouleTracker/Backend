package com.jouletracker.platform.alerts.domain.model.valueobjects;

import lombok.Getter;

/**
 * Value Object representing the severity level of a consumption alert.
 */
@Getter
public enum AlertSeverity {
    INFO("info"),
    WARNING("warning"),
    CRITICAL("critical");

    private final String code;

    AlertSeverity(String code) {
        this.code = code;
    }

    public static AlertSeverity fromString(String value) {
        if (value == null || value.isBlank()) {
            return WARNING;
        }
        for (AlertSeverity severity : values()) {
            if (severity.name().equalsIgnoreCase(value.trim()) || severity.code.equalsIgnoreCase(value.trim())) {
                return severity;
            }
        }
        return WARNING;
    }
}
