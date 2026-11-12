package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.energy.application.queryservices.ComparativeConsumptionQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.ComparativeConsumption;
import com.jouletracker.platform.energy.domain.model.queries.GetComparativeConsumptionQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.jouletracker.platform.energy.application.internal.queryservices.ConsumptionSummaryQueryServiceImpl.*;

@Service
public class ComparativeConsumptionQueryServiceImpl implements ComparativeConsumptionQueryService {

    private final EnergyMeasurementRepository measurementRepository;

    public ComparativeConsumptionQueryServiceImpl(EnergyMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparativeConsumption> handle(GetComparativeConsumptionQuery query) {
        ConsumptionPeriod period = query.period() != null ? query.period() : ConsumptionPeriod.WEEKLY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = resolvePeriodStart(now, period);
        LocalDateTime previousStart = resolvePreviousPeriodStart(currentStart, period);

        List<EnergyMeasurement> currentMeasurements = measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(query.userId(), currentStart, now);
        List<EnergyMeasurement> previousMeasurements = measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(query.userId(), previousStart, currentStart);

        int buckets = switch (period) {
            case DAILY -> 6;
            case WEEKLY -> 7;
            case MONTHLY -> 4;
            case YEARLY -> 12;
        };

        List<ComparativeConsumption> result = new ArrayList<>();
        for (int i = 0; i < buckets; i++) {
            final int bucketIndex = i;
            double currentKwh = round2(currentMeasurements.stream()
                    .filter(m -> resolveBucket(m.getMeasuredAt(), period, buckets) == bucketIndex)
                    .mapToDouble(EnergyMeasurement::getEnergyKwh)
                    .sum());
            double previousKwh = round2(previousMeasurements.stream()
                    .filter(m -> resolveBucket(m.getMeasuredAt(), period, buckets) == bucketIndex)
                    .mapToDouble(EnergyMeasurement::getEnergyKwh)
                    .sum());

            double diffPct = previousKwh > 0 ? round2(((currentKwh - previousKwh) / previousKwh) * 100.0) : 0.0;

            result.add(new ComparativeConsumption(
                    buildLabel(period, bucketIndex),
                    currentKwh,
                    previousKwh,
                    EnergyMeasurement.calculateCost(currentKwh),
                    EnergyMeasurement.calculateCost(previousKwh),
                    diffPct
            ));
        }

        return result;
    }

    private int resolveBucket(LocalDateTime dt, ConsumptionPeriod period, int buckets) {
        return switch (period) {
            case DAILY -> Math.min(buckets - 1, dt.getHour() / 4);
            case WEEKLY -> dt.getDayOfWeek().getValue() - 1;
            case MONTHLY -> Math.min(buckets - 1, (dt.getDayOfMonth() - 1) / 8);
            case YEARLY -> dt.getMonthValue() - 1;
        };
    }

    private String buildLabel(ConsumptionPeriod period, int index) {
        return switch (period) {
            case DAILY -> String.format("%02d:00", index * 4);
            case WEEKLY -> List.of("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").get(index);
            case MONTHLY -> "Sem " + (index + 1);
            case YEARLY -> List.of("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic").get(index);
        };
    }
}