package com.jouletracker.platform.devices.application.acl;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import com.jouletracker.platform.devices.interfaces.acl.DeviceTelemetrySnapshot;
import com.jouletracker.platform.devices.interfaces.acl.DevicesContextFacade;
import com.jouletracker.platform.devices.interfaces.acl.SensorThresholdSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTelemetrySnapshot> fetchDevicesByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return deviceRepository.findAllByUserId(userId).stream()
                .map(device -> new DeviceTelemetrySnapshot(
                        device.getId(),
                        device.getUserId(),
                        device.getName(),
                        device.getLocation(),
                        device.getCategory(),
                        device.getStatus() == DeviceStatus.ONLINE,
                        device.getCurrentPowerKw() != null ? device.getCurrentPowerKw() : 0.0,
                        device.getPowerLimitKw(),
                        device.getTodayKwh() != null ? device.getTodayKwh() : 0.0
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorThresholdSnapshot> fetchSensorThresholdsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return sensorRepository.findAllByUserId(userId).stream()
                .map(this::toSensorThresholdSnapshot)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SensorThresholdSnapshot> fetchSensorThresholdById(Long sensorId) {
        if (sensorId == null) {
            return Optional.empty();
        }
        return sensorRepository.findById(sensorId)
                .map(this::toSensorThresholdSnapshot);
    }

    private SensorThresholdSnapshot toSensorThresholdSnapshot(Sensor sensor) {
        Double devicePowerLimitKw = null;
        String resolvedDeviceName = sensor.getAssignedDeviceName();
        if (sensor.getAssignedDeviceId() != null) {
            Optional<Device> deviceOpt = deviceRepository.findById(sensor.getAssignedDeviceId());
            if (deviceOpt.isPresent()) {
                devicePowerLimitKw = deviceOpt.get().getPowerLimitKw();
                if (resolvedDeviceName == null || resolvedDeviceName.isBlank()) {
                    resolvedDeviceName = deviceOpt.get().getName();
                }
            }
        }
        return new SensorThresholdSnapshot(
                sensor.getId(),
                sensor.getUserId(),
                sensor.getName(),
                sensor.getAssignedDeviceId(),
                resolvedDeviceName,
                sensor.getCurrentPowerKw() != null ? sensor.getCurrentPowerKw() : 0.0,
                sensor.getTodayKwh() != null ? sensor.getTodayKwh() : 0.0,
                sensor.getRecommendedDailyKwh(),
                devicePowerLimitKw
        );
    }
}
