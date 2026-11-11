package com.jouletracker.platform.devices.application.acl;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DevicesContextFacadeImpl implements DevicesContextFacade {

    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;

    public DevicesContextFacadeImpl(SensorRepository sensorRepository, DeviceRepository deviceRepository) {
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public boolean existsSensorById(Long sensorId) {
        return sensorRepository.findById(sensorId).isPresent();
    }

    @Override
    public boolean existsDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId).isPresent();
    }

    @Override
    public boolean updateSensorTelemetry(Long sensorId, Double powerKw, Double addedKwh, String timestamp) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(sensorId);
        if (sensorOpt.isEmpty()) {
            return false;
        }

        Sensor sensor = sensorOpt.get();
        sensor.recordTelemetry(powerKw, addedKwh, timestamp);
        sensorRepository.save(sensor);

        // Also update linked device telemetry if applicable
        if (sensor.getAssignedDeviceId() != null) {
            deviceRepository.findById(sensor.getAssignedDeviceId()).ifPresent(device -> {
                device.updateTelemetry(powerKw, sensor.getTodayKwh(), timestamp);
                deviceRepository.save(device);
            });
        }

        return true;
    }

    @Override
    public Optional<Long> fetchAssignedDeviceIdBySensorId(Long sensorId) {
        return sensorRepository.findById(sensorId)
                .map(Sensor::getAssignedDeviceId);
    }
}
