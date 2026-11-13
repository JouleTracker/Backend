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
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "dia", "día" -> DAILY;
            case "semana" -> WEEKLY;
            case "mes" -> MONTHLY;
            case "ano", "año" -> YEARLY;
            default -> Arrays.stream(values())
                    .filter(p -> p.name().equalsIgnoreCase(normalized) || p.code.equalsIgnoreCase(normalized))
                    .findFirst()
                    .orElse(DAILY);
        };
    }
}