
package com.jouletracker.platform.energy.interfaces.acl;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

/**
 * ACL exposing energy consumption information
 * to other bounded contexts.
 *
 * US-26: Global monthly energy consumption.
 */
@Service
@Transactional(readOnly = true)
public class EnergyContextFacade {

    private final EnergyMeasurementRepository
            measurementRepository;

    public EnergyContextFacade(
            EnergyMeasurementRepository measurementRepository) {

        this.measurementRepository = measurementRepository;
    }

    /**
     * Returns the accumulated energy consumption
     * for the current calendar month.
     */
    public BigDecimal fetchMonthlyConsumptionKwh(Long userId) {

        return fetchMonthlyConsumptionKwh(
                userId,
                LocalDateTime.now()
        );
    }

    /**
     * Returns accumulated consumption from the beginning
     * of the reference month until the reference time.
     */
    public BigDecimal fetchMonthlyConsumptionKwh(
            Long userId,
            LocalDateTime reference) {

        if (userId == null || userId <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }

        Objects.requireNonNull(
                reference,
                "Reference date must not be null"
        );

        LocalDateTime monthStart =
                YearMonth.from(reference)
                        .atDay(1)
                        .atStartOfDay();

        return measurementRepository
                .findAllByUserIdAndMeasuredAtBetween(
                        userId,
                        monthStart,
                        reference
                )
                .stream()
                .filter(measurement ->
                        Objects.equals(
                                measurement.getUserId(),
                                userId
                        )
                )
                .map(EnergyMeasurement::getEnergyKwh)
                .filter(Objects::nonNull)
                .filter(value ->
                        Double.isFinite(value)
                                && value >= 0
                )
                .map(BigDecimal::valueOf)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}
