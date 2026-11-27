
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.MonthlyConsumptionThreshold;
import com.jouletracker.platform.alerts.domain.repositories.MonthlyConsumptionThresholdRepository;
import com.jouletracker.platform.energy.interfaces.acl.EnergyContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

/**
 * US-26: Application service for global monthly
 * energy consumption thresholds.
 */
@Service
@Transactional(readOnly = true)
public class MonthlyConsumptionThresholdService {

    private final MonthlyConsumptionThresholdRepository
            thresholdRepository;

    private final EnergyContextFacade energyContextFacade;

    public MonthlyConsumptionThresholdService(
            MonthlyConsumptionThresholdRepository thresholdRepository,
            EnergyContextFacade energyContextFacade) {

        this.thresholdRepository = thresholdRepository;

        this.energyContextFacade = energyContextFacade;
    }

    public Optional<MonthlyConsumptionThreshold> getThreshold(
            Long userId) {

        validateUserId(userId);

        return thresholdRepository.findByUserId(userId);
    }

    @Transactional
    public MonthlyConsumptionThreshold updateThreshold(
            Long userId,
            BigDecimal monthlyLimitKwh) {

        validateUserId(userId);

        MonthlyConsumptionThreshold threshold =
                thresholdRepository.findByUserId(userId)
                        .orElseGet(() ->
                                new MonthlyConsumptionThreshold(
                                        userId,
                                        monthlyLimitKwh
                                )
                        );

        threshold.updateMonthlyLimit(monthlyLimitKwh);

        return thresholdRepository.save(threshold);
    }

    public boolean isThresholdReached(
            Long userId,
            BigDecimal currentConsumptionKwh) {

        return getThreshold(userId)
                .map(threshold ->
                        threshold.isThresholdReached(
                                currentConsumptionKwh
                        )
                )
                .orElse(false);
    }

    public Optional<BigDecimal> calculateConsumptionPercentage(
            Long userId,
            BigDecimal currentConsumptionKwh) {

        return getThreshold(userId)
                .map(threshold ->
                        threshold.calculateConsumptionPercentage(
                                currentConsumptionKwh
                        )
                );
    }

    /**
     * Returns the current consumption status.
     */
    public MonthlyConsumptionStatus getConsumptionStatus(
            Long userId) {

        return getConsumptionStatus(
                userId,
                LocalDateTime.now()
        );
    }

    /**
     * Evaluates the user's accumulated monthly
     * consumption against the configured threshold.
     */
    public MonthlyConsumptionStatus getConsumptionStatus(
            Long userId,
            LocalDateTime reference) {

        validateUserId(userId);

        var threshold = getThreshold(userId);

        BigDecimal currentConsumption =
                energyContextFacade.fetchMonthlyConsumptionKwh(
                        userId,
                        reference
                );

        String period = YearMonth.from(reference).toString();

        if (threshold.isEmpty()) {

            return new MonthlyConsumptionStatus(
                    userId,
                    period,
                    false,
                    null,
                    currentConsumption,
                    null,
                    null,
                    false
            );
        }

        var configuredThreshold = threshold.get();

        BigDecimal monthlyLimit =
                configuredThreshold.getMonthlyLimitKwh();

        BigDecimal remainingKwh =
                monthlyLimit.subtract(currentConsumption)
                        .max(BigDecimal.ZERO);

        BigDecimal percentage =
                configuredThreshold
                        .calculateConsumptionPercentage(
                                currentConsumption
                        );

        boolean thresholdReached =
                configuredThreshold.isThresholdReached(
                        currentConsumption
                );

        return new MonthlyConsumptionStatus(
                userId,
                period,
                true,
                monthlyLimit,
                currentConsumption,
                remainingKwh,
                percentage,
                thresholdReached
        );
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }
    }

    /**
     * Read model representing the user's monthly
     * consumption and configured threshold.
     */
    public record MonthlyConsumptionStatus(
            Long userId,
            String period,
            boolean configured,
            BigDecimal monthlyLimitKwh,
            BigDecimal currentConsumptionKwh,
            BigDecimal remainingKwh,
            BigDecimal consumptionPercentage,
            boolean thresholdReached
    ) {
    }
}
