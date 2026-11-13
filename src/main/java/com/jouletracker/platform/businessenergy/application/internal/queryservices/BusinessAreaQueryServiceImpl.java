package com.jouletracker.platform.businessenergy.application.internal.queryservices;

import com.jouletracker.platform.businessenergy.application.queryservices.BusinessAreaQueryService;
import com.jouletracker.platform.businessenergy.domain.model.aggregates.BusinessArea;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessAreaMetrics;
import com.jouletracker.platform.businessenergy.domain.model.entities.BusinessDashboardMetrics;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetAllBusinessAreasQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessAreaMetricsByIdQuery;
import com.jouletracker.platform.businessenergy.domain.model.queries.GetBusinessDashboardMetricsQuery;
import com.jouletracker.platform.businessenergy.domain.repositories.BusinessAreaRepository;
import com.jouletracker.platform.devices.interfaces.acl.DeviceTelemetrySnapshot;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Application service implementation for business area queries and MYPE dashboard metrics (TS-17).
 */
@Service
@Transactional(readOnly = true)
public class BusinessAreaQueryServiceImpl implements BusinessAreaQueryService {

    private static final double DEFAULT_TARIFF_PEN_PER_KWH = 0.70;

    private final BusinessAreaRepository businessAreaRepository;
    private final DevicesContextFacade devicesContextFacade;
    private final EnergyMeasurementRepository energyMeasurementRepository;

    public BusinessAreaQueryServiceImpl(BusinessAreaRepository businessAreaRepository,
                                        DevicesContextFacade devicesContextFacade,
                                        EnergyMeasurementRepository energyMeasurementRepository) {
        this.businessAreaRepository = businessAreaRepository;
        this.devicesContextFacade = devicesContextFacade;
        this.energyMeasurementRepository = energyMeasurementRepository;
    }

    @Override
    public List<BusinessArea> handle(GetAllBusinessAreasQuery query) {
        Long resolvedUserId = query.userId() != null ? query.userId() : 1L;
        return businessAreaRepository.findAllByUserId(resolvedUserId);
    }

    @Override
    public Optional<BusinessArea> handle(GetBusinessAreaByIdQuery query) {
        if (query.id() == null) {
            return Optional.empty();
        }
        return businessAreaRepository.findById(query.id());
    }

    @Override
    public BusinessDashboardMetrics handle(GetBusinessDashboardMetricsQuery query) {
        Long resolvedUserId = query.userId() != null ? query.userId() : 1L;
        ConsumptionPeriod period = ConsumptionPeriod.fromString(query.period());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = resolvePeriodStart(now, period);

        List<BusinessArea> areas = businessAreaRepository.findAllByUserId(resolvedUserId);
        List<DeviceTelemetrySnapshot> userDevices = devicesContextFacade.fetchDevicesByUserId(resolvedUserId);

        List<BusinessAreaMetrics> areaMetrics = areas.stream()
                .map(area -> computeAreaMetrics(area, userDevices, period, periodStart, now))
                .toList();

        double totalPowerKw = round2(areaMetrics.stream().mapToDouble(BusinessAreaMetrics::currentPowerKw).sum());
        double totalTodayKwh = round2(areaMetrics.stream().mapToDouble(BusinessAreaMetrics::todayKwh).sum());
        double totalTodayCostSoles = round2(totalTodayKwh * DEFAULT_TARIFF_PEN_PER_KWH);

        String highestArea = areaMetrics.stream()
                .filter(m -> m.todayKwh() > 0)
                .max(Comparator.comparingDouble(BusinessAreaMetrics::todayKwh))
                .map(BusinessAreaMetrics::name)
                .orElse("Sin consumo registrado");

        return new BusinessDashboardMetrics(
                resolvedUserId,
                period.getCode(),
                areas.size(),
                totalPowerKw,
                totalTodayKwh,
                totalTodayCostSoles,
                highestArea,
                areaMetrics
        );
    }

    @Override
    public Optional<BusinessAreaMetrics> handle(GetBusinessAreaMetricsByIdQuery query) {
        if (query.areaId() == null) {
            return Optional.empty();
        }
        Optional<BusinessArea> areaOpt = businessAreaRepository.findById(query.areaId());
        if (areaOpt.isEmpty()) {
            return Optional.empty();
        }
        BusinessArea area = areaOpt.get();
        ConsumptionPeriod period = ConsumptionPeriod.fromString(query.period());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = resolvePeriodStart(now, period);
        List<DeviceTelemetrySnapshot> userDevices = devicesContextFacade.fetchDevicesByUserId(area.getUserId());

        return Optional.of(computeAreaMetrics(area, userDevices, period, periodStart, now));
    }

    private BusinessAreaMetrics computeAreaMetrics(BusinessArea area,
                                                   List<DeviceTelemetrySnapshot> userDevices,
                                                   ConsumptionPeriod period,
                                                   LocalDateTime periodStart,
                                                   LocalDateTime now) {
        List<DeviceTelemetrySnapshot> areaDevices = userDevices.stream()
                .filter(device -> matchesArea(device, area))
                .toList();

        int deviceCount = areaDevices.size();
        int onlineDeviceCount = (int) areaDevices.stream().filter(DeviceTelemetrySnapshot::online).count();
        double currentPowerKw = round2(areaDevices.stream()
                .mapToDouble(d -> d.currentPowerKw() != null ? d.currentPowerKw() : 0.0)
                .sum());

        double areaKwh = round2(areaDevices.stream()
                .mapToDouble(d -> resolveDeviceKwh(area.getUserId(), d, period, periodStart, now))
                .sum());
        double areaCost = round2(areaKwh * DEFAULT_TARIFF_PEN_PER_KWH);

        return new BusinessAreaMetrics(
                area.getId(),
                area.getUserId(),
                area.getName(),
                area.getFloor(),
                area.getDescription(),
                area.getIcon(),
                deviceCount,
                onlineDeviceCount,
                currentPowerKw,
                areaKwh,
                areaCost,
                deviceCount > 0
        );
    }

    private double resolveDeviceKwh(Long userId,
                                    DeviceTelemetrySnapshot device,
                                    ConsumptionPeriod period,
                                    LocalDateTime periodStart,
                                    LocalDateTime now) {
        if (period == ConsumptionPeriod.DAILY) {
            return device.todayKwh() != null ? device.todayKwh() : 0.0;
        }
        if (device.deviceId() == null) {
            return 0.0;
        }
        return energyMeasurementRepository
                .findAllByUserIdAndDeviceIdAndMeasuredAtBetween(userId, device.deviceId(), periodStart, now)
                .stream()
                .mapToDouble(m -> m.getEnergyKwh() != null ? m.getEnergyKwh() : 0.0)
                .sum();
    }

    private boolean matchesArea(DeviceTelemetrySnapshot device, BusinessArea area) {
        if (device.location() == null || device.location().isBlank() || area.getName() == null) {
            return false;
        }
        String locNorm = device.location().trim().toLowerCase();
        String areaNameNorm = area.getName().trim().toLowerCase();
        return !locNorm.isEmpty() && !areaNameNorm.isEmpty()
                && (locNorm.contains(areaNameNorm) || areaNameNorm.contains(locNorm));
    }

    private LocalDateTime resolvePeriodStart(LocalDateTime reference, ConsumptionPeriod period) {
        return switch (period) {
            case DAILY -> reference.toLocalDate().atStartOfDay();
            case WEEKLY -> reference.minusDays(7);
            case MONTHLY -> reference.minusDays(30);
            case YEARLY -> reference.minusDays(365);
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
