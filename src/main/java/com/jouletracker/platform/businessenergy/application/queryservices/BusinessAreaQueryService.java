package com.jouletracker.platform.businessenergy.application.queryservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application query service for retrieving business areas.
 */
public interface BusinessAreaQueryService {

    List<BusinessArea> handle(GetAllBusinessAreasQuery query);

    Optional<BusinessArea> handle(GetBusinessAreaByIdQuery query);
}
