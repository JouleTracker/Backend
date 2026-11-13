package com.jouletracker.platform.energy.domain.model.queries;

import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;

public record GetConsumptionSummaryQuery(Long userId, ConsumptionPeriod period) {
}