
package com.jouletracker.platform.alerts.application.internal.services;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * US-26: Generates global monthly consumption alerts.
 */
@Service
public class MonthlyConsumptionAlertService {

    private final MonthlyConsumptionThresholdService
            thresholdService;

    private final AlertRepository alertRepository;

    public MonthlyConsumptionAlertService(
            MonthlyConsumptionThresholdService thresholdService,
            AlertRepository alertRepository) {

        this.thresholdService = thresholdService;
        this.alertRepository = alertRepository;
    }

    @Transactional
    public Optional<Alert> evaluateMonthlyConsumption(
            Long userId) {

        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        if (thresholdService.getThreshold(userId).isEmpty()) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();

        var status = thresholdService.getConsumptionStatus(
                userId,
                now
        );

        if (!status.configured()
                || !status.thresholdReached()) {

            return Optional.empty();
        }

        String title =
                "Límite mensual de consumo alcanzado: "
                        + status.period();

        // Prevent repeated monthly alerts, including
        // alerts that have already been marked as read.
        boolean alreadyGenerated =
                alertRepository
                        .findAllByUserIdOrderByCreatedAtDesc(userId)
                        .stream()
                        .anyMatch(alert ->
                                Objects.equals(
                                        alert.getUserId(),
                                        userId
                                )
                                        && alert.getSensorId() == null
                                        && Objects.equals(
                                        alert.getTitle(),
                                        title
                                )
                        );

        if (alreadyGenerated) {
            return Optional.empty();
        }

        String description = String.format(
                "Tu consumo mensual ha alcanzado %s kWh. "
                        + "El límite configurado es de %s kWh. "
                        + "Has utilizado el %s%% de tu límite mensual.",
                status.currentConsumptionKwh()
                        .stripTrailingZeros()
                        .toPlainString(),
                status.monthlyLimitKwh()
                        .stripTrailingZeros()
                        .toPlainString(),
                status.consumptionPercentage()
                        .toPlainString()
        );

        Alert alert = new Alert(
                null,
                userId,
                null,
                null,
                title,
                description,
                AlertSeverity.WARNING,
                "/configuracion",
                false,
                AlertStatus.ACTIVE,
                now
        );

        return Optional.of(
                alertRepository.save(alert)
        );
    }
}
