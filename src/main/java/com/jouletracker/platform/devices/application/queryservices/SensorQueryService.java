package com.jouletracker.platform.devices.application.queryservices;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.entities.SensorSummary;
import com.jouletracker.platform.devices.domain.model.queries.GetAllSensorsByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorByIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetSensorSummaryByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface SensorQueryService {
    Optional<Sensor> handle(GetSensorByIdQuery query);
    List<Sensor> handle(GetAllSensorsByUserIdQuery query);
    SensorSummary handle(GetSensorSummaryByUserIdQuery query);
}
