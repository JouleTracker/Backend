package com.jouletracker.platform.devices.application.internal.eventhandlers;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.events.DeviceDeletedEvent;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event handler that automatically unlinks any IoT sensor assigned to a deleted device (US-44).
 */
@Component
public class DeviceDeletedEventHandler {

    private final SensorRepository sensorRepository;

    public DeviceDeletedEventHandler(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @EventListener
    public void on(DeviceDeletedEvent event) {
        List<Sensor> linkedSensors = sensorRepository.findByAssignedDeviceId(event.deviceId());
        for (Sensor sensor : linkedSensors) {
            sensor.unassignDevice();
            sensorRepository.save(sensor);
        }
    }
}
