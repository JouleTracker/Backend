package com.jouletracker.platform.energy.interfaces.rest.resources;

public record DeviceDistributionResource(
        Long deviceId,
        String deviceName,
        String category,
        Double totalKwh,
        Double totalCost,
        Double percentage
) {
}