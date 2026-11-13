package com.jouletracker.platform.energy.application.internal.queryservices;

import com.jouletracker.platform.energy.application.queryservices.ConsumptionSummaryQueryService;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.model.entities.ConsumptionSummary;
import com.jouletracker.platform.energy.domain.model.queries.GetConsumptionSummaryQuery;
import com.jouletracker.platform.energy.domain.model.valueobjects.ConsumptionPeriod;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ConsumptionSummaryQueryServiceImpl implements ConsumptionSummaryQueryService {

    private final EnergyMeasurementRepository measurementRepository;

    public ConsumptionSummaryQueryServiceImpl(EnergyMeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ConsumptionSummary handle(GetConsumptionSummaryQuery query) {
        ConsumptionPeriod period = query.period() != null ? query.period() : ConsumptionPeriod.DAILY;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart = resolvePeriodStart(now, period);
        LocalDateTime previousStart = resolvePreviousPeriodStart(currentStart, period);

        List<EnergyMeasurement> currentMeasurements = measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(query.userId(), currentStart, now);
        List<EnergyMeasurement> previousMeasurements = measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(query.userId(), previousStart, currentStart);

        double totalKwh = round2(currentMeasurements.stream().mapToDouble(EnergyMeasurement::getEnergyKwh).sum());
        double previousKwh = previousMeasurements.stream().mapToDouble(EnergyMeasurement::getEnergyKwh).sum();
        double totalCost = EnergyMeasurement.calculateCost(totalKwh);

        double averagePowerKw = round2(currentMeasurements.stream()
                .mapToDouble(EnergyMeasurement::getPowerKw)
                .average()
                .orElse(0.0));
        double peakPowerKw = round2(currentMeasurements.stream()
                .mapToDouble(EnergyMeasurement::getPowerKw)
                .max()
                .orElse(0.0));

        double variationPercentage = 0.0;
        if (previousKwh > 0) {
            variationPercentage = round2(((totalKwh - previousKwh) / previousKwh) * 100.0);
        }

        double kwhSaved = previousKwh > totalKwh ? (previousKwh - totalKwh) : 0.0;
        double avoidedEmissionsKg = EnergyMeasurement.calculateAvoidedEmissionsKg(kwhSaved);

        int activeDevicesCount = (int) currentMeasurements.stream()
                .map(EnergyMeasurement::getDeviceId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return new ConsumptionSummary(
                query.userId(),
                period,
                totalKwh,
                totalCost,
                averagePowerKw,
                peakPowerKw,
                variationPercentage,
                avoidedEmissionsKg,
                activeDevicesCount
        );
    }

    static LocalDateTime resolvePeriodStart(LocalDateTime reference, ConsumptionPeriod period) {
        return switch (period) {
            case DAILY -> reference.toLocalDate().atStartOfDay();
            case WEEKLY -> reference.minusDays(7);
            case MONTHLY -> reference.minusDays(30);
            case YEARLY -> reference.minusDays(365);
        };
    }

    static LocalDateTime resolvePreviousPeriodStart(LocalDateTime currentStart, ConsumptionPeriod period) {
        return switch (period) {
            case DAILY -> currentStart.minusDays(1);
            case WEEKLY -> currentStart.minusDays(7);
            case MONTHLY -> currentStart.minusDays(30);
            case YEARLY -> currentStart.minusDays(365);
        };
    }

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}