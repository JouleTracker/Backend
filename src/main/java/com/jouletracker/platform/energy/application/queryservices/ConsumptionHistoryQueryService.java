package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.entities.ConsumptionHistory;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;

import java.util.List;

public interface ConsumptionHistoryQueryService {
    List<ConsumptionHistory> handle(GetConsumptionHistoryQuery query);
}