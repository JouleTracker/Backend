package com.jouletracker.platform.energy.interfaces.rest.resources;

import java.time.LocalDateTime;

public record ConsumptionHistoryResource(
        Long id,
        Long deviceId,
        String deviceName,
        String category,
        Double powerKw,
        Double energyKwh,
        Double cost,
        String status,
        LocalDateTime recordedAt
) {
}