package com.jouletracker.platform.energy.domain.model.queries;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

import java.time.LocalDateTime;

public record GetConsumptionHistoryQuery(
        Long userId,
        ConsumptionPeriod period,
        Long deviceId,
        LocalDateTime from,
        LocalDateTime to
) {
    public GetConsumptionHistoryQuery(Long userId, ConsumptionPeriod period, Long deviceId) {
        this(userId, period, deviceId, null, null);
    }
}