package com.jouletracker.platform.alerts.application.internal.eventhandlers;

import com.jouletracker.platform.alerts.application.commandservices.AlertCommandService;
import com.jouletracker.platform.alerts.domain.model.commands.EvaluateConsumptionAlertCommand;
import com.jouletracker.platform.energy.domain.model.events.EnergyMeasurementRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for registered energy measurements and evaluates consumption/power thresholds (TS-14).
 */
@Component
public class EnergyMeasurementAlertEventHandler {

    private final AlertCommandService alertCommandService;

    public EnergyMeasurementAlertEventHandler(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @EventListener
    public void on(EnergyMeasurementRegisteredEvent event) {
        if (event == null || event.userId() == null) {
            return;
        }
        alertCommandService.handle(new EvaluateConsumptionAlertCommand(
                event.userId(),
                event.sensorId(),
                event.deviceId(),
                event.deviceName(),
                event.powerKw(),
                event.energyKwh()
        ));
    }
}
