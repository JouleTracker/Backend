package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionHistoryQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.ConsumptionHistory;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionHistoryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.resolvePeriodStart;
import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.round2;

@Service
public class ConsumptionHistoryQueryServiceImpl implements ConsumptionHistoryQueryService {

    private final EnergyMeasurementRepository measurementRepository;

    public ConsumptionHistoryQueryServiceImpl(EnergyMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumptionHistory> handle(GetConsumptionHistoryQuery query) {
        ConsumptionPeriod period = query.period() != null ? query.period() : ConsumptionPeriod.MONTHLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = resolvePeriodStart(now, period);

        List<EnergyMeasurement> measurements = query.deviceId() != null
                ? measurementRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetween(query.userId(), query.deviceId(), start, now)
                : measurementRepository.findAllByUserIdAndMeasuredAtBetween(query.userId(), start, now);

        return measurements.stream()
                .sorted(Comparator.comparing(EnergyMeasurement::getMeasuredAt).reversed())
                .map(m -> new ConsumptionHistory(
                        m.getId(),
                        m.getDeviceId(),
                        m.getDeviceName(),
                        m.getDeviceCategory(),
                        round2(m.getPowerKw()),
                        round2(m.getEnergyKwh()),
                        round2(m.getCost()),
                        classifyStatus(m.getPowerKw()),
                        m.getMeasuredAt()
                ))
                .toList();
    }

    private String classifyStatus(Double powerKw) {
        if (powerKw == null || powerKw <= 0.0) {
            return "Apagado";
        }
        if (powerKw < 0.05) {
            return "Standby";
        }
        if (powerKw > 2.0) {
            return "Pico Alto";
        }
        return "Normal";
    }
}