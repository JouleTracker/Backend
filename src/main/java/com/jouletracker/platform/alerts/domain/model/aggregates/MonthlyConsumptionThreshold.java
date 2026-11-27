
package com.jouletracker.platform.alerts.domain.model.aggregates;

import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * US-26: Aggregate root representing the global
 * monthly energy consumption threshold of a user.
 */
@Getter
public class MonthlyConsumptionThreshold
        extends AbstractDomainAggregateRoot<MonthlyConsumptionThreshold> {

    private final Long userId;

    private BigDecimal monthlyLimitKwh;

    public MonthlyConsumptionThreshold(
            Long userId,
            BigDecimal monthlyLimitKwh) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }

        this.userId = userId;

        updateMonthlyLimit(monthlyLimitKwh);
    }

    /**
     * Updates the global monthly consumption limit.
     */
    public void updateMonthlyLimit(BigDecimal monthlyLimitKwh) {

        if (monthlyLimitKwh == null
                || monthlyLimitKwh.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Monthly consumption limit must be greater than zero"
            );
        }

        BigDecimal normalizedLimit =
                monthlyLimitKwh.stripTrailingZeros();

        if (normalizedLimit.scale() > 3) {
            throw new IllegalArgumentException(
                    "Monthly limit supports up to three decimal places"
            );
        }

        if (normalizedLimit.compareTo(
                new BigDecimal("999999999.999")) > 0) {

            throw new IllegalArgumentException(
                    "Monthly consumption limit exceeds the maximum value"
            );
        }

        this.monthlyLimitKwh =
                normalizedLimit.setScale(3, RoundingMode.UNNECESSARY);
    }

    /**
     * Determines whether the monthly threshold
     * has been reached or exceeded.
     */
    public boolean isThresholdReached(
            BigDecimal currentConsumptionKwh) {

        validateConsumption(currentConsumptionKwh);

        return currentConsumptionKwh
                .compareTo(monthlyLimitKwh) >= 0;
    }

    /**
     * Calculates the percentage of monthly
     * consumption relative to the configured limit.
     */
    public BigDecimal calculateConsumptionPercentage(
            BigDecimal currentConsumptionKwh) {

        validateConsumption(currentConsumptionKwh);

        return currentConsumptionKwh
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        monthlyLimitKwh,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void validateConsumption(
            BigDecimal currentConsumptionKwh) {

        if (currentConsumptionKwh == null
                || currentConsumptionKwh.signum() < 0) {

            throw new IllegalArgumentException(
                    "Current consumption must not be negative or null"
            );
        }
    }
}

