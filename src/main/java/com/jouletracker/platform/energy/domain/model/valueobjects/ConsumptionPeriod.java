package com.jouletracker.platform.energy.domain.model.valueobjects;

import lombok.Getter;

import java.util.Arrays;

/**
 * Value Object representing the time period granularity for energy consumption queries.
 */
@Getter
public enum ConsumptionPeriod {
    DAILY("daily", "Diario"),
    WEEKLY("weekly", "Semanal"),
    MONTHLY("monthly", "Mensual"),
    YEARLY("yearly", "Anual");

    private final String code;
    private final String displayName;

    ConsumptionPeriod(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static ConsumptionPeriod fromString(String value) {
        if (value == null || value.isBlank()) {
            return DAILY;
        }
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(value.trim()) || p.code.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(DAILY);
    }
}