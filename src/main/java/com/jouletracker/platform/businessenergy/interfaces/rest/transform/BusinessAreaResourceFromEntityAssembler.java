package com.jouletracker.platform.businessenergy.interfaces.rest.transform;

import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaMetricsResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessAreaResource;
import com.jouletracker.platform.businessenergy.interfaces.rest.resources.BusinessDashboardMetricsResource;

import java.util.List;

/**
 * Assembler to translate BusinessArea aggregates and area metrics into REST response resources.
 */
public final class BusinessAreaResourceFromEntityAssembler {

    private BusinessAreaResourceFromEntityAssembler() {
    }

    public static BusinessAreaResource toResourceFromEntity(BusinessArea entity) {
        if (entity == null) {
            return null;
        }

        return new BusinessAreaResource(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getFloor(),
                entity.getDescription(),
                entity.getIcon()
        );
    }

    public static BusinessAreaMetricsResource toMetricsResourceFromEntity(BusinessAreaMetrics entity) {
        if (entity == null) {
            return null;
        }

        return new BusinessAreaMetricsResource(
                entity.areaId(),
                entity.userId(),
                entity.name(),
                entity.floor(),
                entity.description(),
                entity.icon(),
                entity.deviceCount(),
                entity.onlineDeviceCount(),
                entity.currentPowerKw(),
                entity.todayKwh(),
                entity.todayCostSoles(),
                entity.hasDevices()
        );
    }

    public static BusinessDashboardMetricsResource toDashboardResourceFromEntity(BusinessDashboardMetrics entity) {
        if (entity == null) {
            return null;
        }

        List<BusinessAreaMetricsResource> areas = entity.areas() != null
                ? entity.areas().stream().map(BusinessAreaResourceFromEntityAssembler::toMetricsResourceFromEntity).toList()
                : List.of();

        return new BusinessDashboardMetricsResource(
                entity.userId(),
                entity.period(),
                entity.totalAreas(),
                entity.totalPowerKw(),
                entity.totalTodayKwh(),
                entity.totalTodayCostSoles(),
                entity.highestConsumptionAreaName(),
                areas
        );
    }
}
