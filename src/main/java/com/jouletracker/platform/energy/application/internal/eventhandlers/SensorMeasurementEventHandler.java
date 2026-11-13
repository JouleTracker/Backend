package com.jouletracker.platform.energy.application.internal.eventhandlers;

import com.jouletracker.platform.devices.domain.model.events.SensorRegisteredEvent;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.energy.application.commandservices.EnergyMeasurementCommandService;
import com.jouletracker.platform.energy.domain.model.commands.RegisterEnergyMeasurementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Event handler reacting to IoT sensor lifecycle and telemetry events to initialize baseline measurements.
 */
@Component
public class SensorMeasurementEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMeasurementEventHandler.class);

    private final EnergyMeasurementCommandService commandService;
    private final DevicesContextFacade devicesContextFacade;

    public SensorMeasurementEventHandler(EnergyMeasurementCommandService commandService,
                                         DevicesContextFacade devicesContextFacade) {
        this.commandService = commandService;
        this.devicesContextFacade = devicesContextFacade;
    }

    @EventListener
    public void on(SensorRegisteredEvent event) {
        if (event == null || event.userId() == null) {
            return;
        }
        Long deviceId = devicesContextFacade.fetchAssignedDeviceIdBySensorId(event.sensorId()).orElse(null);
        LOGGER.info("Initializing baseline energy measurement for registered sensor id={}", event.sensorId());
        var command = new RegisterEnergyMeasurementCommand(
                event.userId(),
                event.sensorId(),
                deviceId,
                "Sensor IoT #" + event.sensorId(),
                "Monitoreo IoT",
                0.0,
                0.0,
                220.0,
                0.0,
                LocalDateTime.now()
        );
        commandService.handle(command);
    }
}
