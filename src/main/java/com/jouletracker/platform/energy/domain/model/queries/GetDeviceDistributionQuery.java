package com.jouletracker.platform.energy.domain.model.queries;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

public record GetDeviceDistributionQuery(Long userId, ConsumptionPeriod period) {
}