package com.jouletracker.platform.devices.application.queryservices;

import com.jouletracker.platform.devices.domain.model.entities.ApplianceProfile;
import com.jouletracker.platform.devices.domain.model.queries.GetAllApplianceProfilesQuery;

import java.util.List;

public interface ApplianceProfileQueryService {
    List<ApplianceProfile> handle(GetAllApplianceProfilesQuery query);
}
