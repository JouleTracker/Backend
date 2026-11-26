package com.jouletracker.platform.energy.domain.model.queries;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

public record GetEnergyReadingsQuery(Long userId, ConsumptionPeriod period, Long deviceId) {
}