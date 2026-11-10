package com.jouletracker.platform.devices.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated KPI summary for user devices")
public record DeviceSummaryResource(
        Long id,
        int connectedDevices,
        int waitingDevices,
        double totalPowerKw,
        double powerDiffVsYesterday,
        String powerDiffVsYesterdayLabel,
        double costDiffVsMonth,
        String costDiffVsMonthLabel,
        double savedKwhMonthly,
        double estimatedCostSoles
) {
}
