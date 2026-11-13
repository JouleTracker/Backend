package com.jouletracker.platform.energy.domain.model.entities;

import java.time.LocalDateTime;

/**
 * Domain read-model entity representing an itemized historical consumption record.
 */
public record ConsumptionHistory(
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