package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.entities.ComparativeConsumption;
import com.jouletracker.platform.energy.domain.model.queries.GetComparativeConsumptionQuery;

import java.util.List;

public interface ComparativeConsumptionQueryService {
    List<ComparativeConsumption> handle(GetComparativeConsumptionQuery query);
}