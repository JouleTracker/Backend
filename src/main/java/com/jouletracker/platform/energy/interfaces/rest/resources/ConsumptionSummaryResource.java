package com.jouletracker.platform.energy.interfaces.rest.resources;

public record ConsumptionSummaryResource(
        Long userId,
        String period,
        Double totalKwh,
        Double totalCost,
        Double averagePowerKw,
        Double peakPowerKw,
        Double variationPercentage,
        Double avoidedEmissionsKg,
        Integer activeDevicesCount
) {
}