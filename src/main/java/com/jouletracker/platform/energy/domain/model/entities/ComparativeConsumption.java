package com.jouletracker.platform.energy.domain.model.entities;

/**
 * Domain read-model entity comparing current vs previous period energy consumption.
 */
public record ComparativeConsumption(
        String label,
        Double currentPeriodKwh,
        Double previousPeriodKwh,
        Double currentPeriodCost,
        Double previousPeriodCost,
        Double differencePercentage
) {
}