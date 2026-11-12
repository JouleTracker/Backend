package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.entities.EnergyReading;
import com.jouletracker.platform.energy.domain.model.queries.GetEnergyReadingsQuery;

import java.util.List;

public interface EnergyReadingQueryService {
    List<EnergyReading> handle(GetEnergyReadingsQuery query);
}