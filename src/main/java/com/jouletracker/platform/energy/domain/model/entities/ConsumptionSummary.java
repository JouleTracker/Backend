package com.jouletracker.platform.energy.domain.model.entities;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

/**
 * Domain read-model entity summarizing overall energy consumption KPIs for a user and period.
 */
public record ConsumptionSummary(
        Long userId,
        ConsumptionPeriod period,
        Double totalKwh,
        Double totalCost,
        Double averagePowerKw,
        Double peakPowerKw,
        Double variationPercentage,
        Double avoidedEmissionsKg,
        Integer activeDevicesCount
) {
}