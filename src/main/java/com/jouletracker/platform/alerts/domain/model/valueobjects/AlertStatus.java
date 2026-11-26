package com.jouletracker.platform.alerts.domain.model.valueobjects;

import lombok.Getter;

/**
 * Value Object representing the lifecycle status of a consumption alert.
 */
@Getter
public enum AlertStatus {
    ACTIVE("Activa"),
    READ("Leída");

    private final String displayName;

    AlertStatus(String displayName) {
        this.displayName = displayName;
    }

    public static AlertStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return ACTIVE;
        }
        for (AlertStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim()) || status.displayName.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return ACTIVE;
    }
}
