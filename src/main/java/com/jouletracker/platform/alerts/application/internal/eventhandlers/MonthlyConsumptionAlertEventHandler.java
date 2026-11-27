
package com.jouletracker.platform.alerts.application.internal.eventhandlers;

import com.jouletracker.platform.alerts.application.internal.services.MonthlyConsumptionAlertService;
import com.jouletracker.platform.energy.domain.model.events.EnergyMeasurementRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * US-26: Evaluates monthly consumption after
 * an energy measurement transaction commits.
 */
@Component
public class MonthlyConsumptionAlertEventHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    MonthlyConsumptionAlertEventHandler.class
            );

    private final MonthlyConsumptionAlertService
            monthlyAlertService;

    public MonthlyConsumptionAlertEventHandler(
            MonthlyConsumptionAlertService monthlyAlertService) {

        this.monthlyAlertService = monthlyAlertService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = false
    )
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void on(EnergyMeasurementRegisteredEvent event) {

        if (event == null
                || event.userId() == null
                || event.userId() <= 0
                || event.measuredAt() == null) {

            return;
        }

        LocalDateTime now = LocalDateTime.now();

        if (!YearMonth.from(event.measuredAt())
                .equals(YearMonth.from(now))
                || event.measuredAt().isAfter(now)) {

            return;
        }

        monthlyAlertService
                .evaluateMonthlyConsumption(event.userId())
                .ifPresent(alert ->
                        LOGGER.info(
                                "Monthly consumption alert generated for user {}",
                                event.userId()
                        )
                );
    }
}
