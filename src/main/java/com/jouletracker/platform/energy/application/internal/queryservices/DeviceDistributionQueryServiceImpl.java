package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.energy.application.queryservices.DeviceDistributionQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.DeviceDistribution;
import com.jouletracker.platform.energy.domain.model.queries.GetDeviceDistributionQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.resolvePeriodStart;
import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.round2;

@Service
public class DeviceDistributionQueryServiceImpl implements DeviceDistributionQueryService {

    private final EnergyMeasurementRepository measurementRepository;

    public DeviceDistributionQueryServiceImpl(EnergyMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDistribution> handle(GetDeviceDistributionQuery query) {
        ConsumptionPeriod period = query.period() != null ? query.period() : ConsumptionPeriod.DAILY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = resolvePeriodStart(now, period);

        List<EnergyMeasurement> measurements = measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(query.userId(), start, now);

        double grandTotalKwh = measurements.stream().mapToDouble(EnergyMeasurement::getEnergyKwh).sum();

        Map<String, List<EnergyMeasurement>> groupedByDevice = measurements.stream()
                .collect(Collectors.groupingBy(m -> m.getDeviceName() != null ? m.getDeviceName() : "Dispositivo General"));

        return groupedByDevice.entrySet().stream()
                .map(entry -> {
                    List<EnergyMeasurement> group = entry.getValue();
                    EnergyMeasurement sample = group.getFirst();
                    double deviceKwh = round2(group.stream().mapToDouble(EnergyMeasurement::getEnergyKwh).sum());
                    double deviceCost = EnergyMeasurement.calculateCost(deviceKwh);
                    double percentage = grandTotalKwh > 0 ? round2((deviceKwh / grandTotalKwh) * 100.0) : 0.0;

                    return new DeviceDistribution(
                            sample.getDeviceId(),
                            entry.getKey(),
                            sample.getDeviceCategory(),
                            deviceKwh,
                            deviceCost,
                            percentage
                    );
                })
                .sorted(Comparator.comparingDouble(DeviceDistribution::totalKwh).reversed())
                .toList();
    }
}