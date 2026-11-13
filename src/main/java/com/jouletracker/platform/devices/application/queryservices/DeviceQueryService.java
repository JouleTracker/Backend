package com.jouletracker.platform.devices.application.queryservices;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.entities.DeviceSummary;
import com.jouletracker.platform.devices.domain.model.queries.GetAllDevicesByUserIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceByIdQuery;
import com.jouletracker.platform.devices.domain.model.queries.GetDeviceSummaryByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface DeviceQueryService {
    Optional<Device> handle(GetDeviceByIdQuery query);
    List<Device> handle(GetAllDevicesByUserIdQuery query);
    DeviceSummary handle(GetDeviceSummaryByUserIdQuery query);
}
