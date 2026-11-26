package com.jouletracker.platform.businessenergy.domain.model.entities;

import java.util.List;

/**
 * Domain read-model representing aggregated MYPE dashboard metrics across all business areas (TS-17).
 */
public record BusinessDashboardMetrics(
        Long userId,
        String period,
        int totalAreas,
        double totalPowerKw,
        double totalTodayKwh,
        double totalTodayCostSoles,
        String highestConsumptionAreaName,
        List<BusinessAreaMetrics> areas
) {
}
