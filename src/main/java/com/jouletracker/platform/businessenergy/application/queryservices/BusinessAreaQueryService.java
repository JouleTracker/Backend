package com.jouletracker.platform.businessenergy.application.queryservices;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application query service for retrieving business areas and aggregated area energy metrics.
 */
public interface BusinessAreaQueryService {

    List<BusinessArea> handle(GetAllBusinessAreasQuery query);

    Optional<BusinessArea> handle(GetBusinessAreaByIdQuery query);

    BusinessDashboardMetrics handle(GetBusinessDashboardMetricsQuery query);

    Optional<BusinessAreaMetrics> handle(GetBusinessAreaMetricsByIdQuery query);
}
