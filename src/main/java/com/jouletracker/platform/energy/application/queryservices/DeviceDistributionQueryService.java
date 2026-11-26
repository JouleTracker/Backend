package com.jouletracker.platform.energy.application.queryservices;

import com.jouletracker.platform.energy.domain.model.entities.DeviceDistribution;
import com.jouletracker.platform.energy.domain.model.queries.GetDeviceDistributionQuery;

import java.util.List;

public interface DeviceDistributionQueryService {
    List<DeviceDistribution> handle(GetDeviceDistributionQuery query);
}