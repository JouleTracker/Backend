package com.jouletracker.platform.energy.domain.model.queries;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

public record GetConsumptionHistoryQuery(Long userId, ConsumptionPeriod period, Long deviceId) {
}