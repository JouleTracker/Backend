package com.jouletracker.platform.devices.domain.model.entities;

/**
 * Summary record representing aggregated metrics for devices.
 */
public record DeviceSummary(
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
