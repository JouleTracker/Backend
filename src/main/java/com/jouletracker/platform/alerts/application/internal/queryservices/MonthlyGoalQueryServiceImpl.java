package com.jouletracker.platform.alerts.application.internal.queryservices;

import com.jouletracker.platform.alerts.application.queryservices.MonthlyGoalQueryService;
import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyGoal;
import com.jouletracker.platform.alerts.domain.model.entities.MonthlyGoalProgress;
import com.jouletracker.platform.alerts.domain.model.queries.GetCurrentMonthlyGoalQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalByIdQuery;
import com.jouletracker.platform.alerts.domain.model.queries.GetMonthlyGoalsByUserIdQuery;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyGoalRepository;
import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Application query service computing monthly consumption goal progress (US-28).
 */
@Service
@Transactional(readOnly = true)
public class MonthlyGoalQueryServiceImpl implements MonthlyGoalQueryService {

    private final MonthlyGoalRepository monthlyGoalRepository;
    private final EnergyMeasurementRepository energyMeasurementRepository;

    public MonthlyGoalQueryServiceImpl(MonthlyGoalRepository monthlyGoalRepository,
                                       EnergyMeasurementRepository energyMeasurementRepository) {
        this.monthlyGoalRepository = monthlyGoalRepository;
        this.energyMeasurementRepository = energyMeasurementRepository;
    }

    @Override
    public List<MonthlyGoalProgress> handle(GetMonthlyGoalsByUserIdQuery query) {
        Long resolvedUserId = (query != null && query.userId() != null) ? query.userId() : 1L;
        return monthlyGoalRepository.findAllByUserId(resolvedUserId).stream()
                .map(this::toProgress)
                .toList();
    }

    @Override
    public Optional<MonthlyGoalProgress> handle(GetMonthlyGoalByIdQuery query) {
        if (query == null || query.id() == null) {
            return Optional.empty();
        }
        return monthlyGoalRepository.findById(query.id())
                .map(this::toProgress);
    }

    @Override
    public Optional<MonthlyGoalProgress> handle(GetCurrentMonthlyGoalQuery query) {
        Long resolvedUserId = (query != null && query.userId() != null) ? query.userId() : 1L;
        LocalDate now = LocalDate.now();
        int resolvedYear = (query != null && query.year() != null) ? query.year() : now.getYear();
        int resolvedMonth = (query != null && query.month() != null) ? query.month() : now.getMonthValue();

        return monthlyGoalRepository.findByUserIdAndYearAndMonth(resolvedUserId, resolvedYear, resolvedMonth)
                .map(this::toProgress);
    }

    public MonthlyGoalProgress toProgress(MonthlyGoal goal) {
        YearMonth ym = YearMonth.of(goal.getYear(), goal.getMonth());
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        double currentKwh = round2(energyMeasurementRepository
                .findAllByUserIdAndMeasuredAtBetween(goal.getUserId(), start, end)
                .stream()
                .mapToDouble(m -> m.getEnergyKwh() != null ? m.getEnergyKwh() : 0.0)
                .sum());

        double targetKwh = round2(goal.getTargetKwh());
        double progressPct = targetKwh > 0 ? round2((currentKwh / targetKwh) * 100.0) : 0.0;
        double remainingKwh = round2(Math.max(0.0, targetKwh - currentKwh));
        boolean exceeded = currentKwh > targetKwh;

        return new MonthlyGoalProgress(
                goal.getId(),
                goal.getUserId(),
                targetKwh,
                goal.getYear(),
                goal.getMonth(),
                currentKwh,
                progressPct,
                remainingKwh,
                exceeded
        );
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}