package com.jouletracker.platform.energy.interfaces.rest.resources;

public record ComparativeConsumptionResource(
        String label,
        Double currentPeriodKwh,
        Double previousPeriodKwh,
        Double currentPeriodCost,
        Double previousPeriodCost,
        Double differencePercentage
) {
}