package com.jouletracker.platform.businessenergy.application.queryservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;

import java.util.List;

/**
 * Application query service for retrieving business areas.
 */
public interface BusinessAreaQueryService {

    List<BusinessArea> handle(GetAllBusinessAreasQuery query);
}
