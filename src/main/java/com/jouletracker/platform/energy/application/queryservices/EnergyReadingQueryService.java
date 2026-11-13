package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.EnergyReading;
import com.jouletracker.platform.energy.domain.model.queries.GetEnergyReadingsQuery;
import com.jouletracker.platform.energy.domain.model.queries.GetLatestReadingBySensorIdQuery;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;

import java.util.List;

public interface EnergyReadingQueryService {
    List<EnergyReading> handle(GetEnergyReadingsQuery query);
    Result<EnergyMeasurement, ApplicationError> handle(GetLatestReadingBySensorIdQuery query);
}
