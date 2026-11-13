package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.entities.ConsumptionSummary;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionSummaryQuery;

public interface ConsumptionSummaryQueryService {
    ConsumptionSummary handle(GetConsumptionSummaryQuery query);
}