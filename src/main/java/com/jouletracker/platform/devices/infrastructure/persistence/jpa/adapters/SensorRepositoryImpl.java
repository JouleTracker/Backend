package com.jouletracker.platform.devices.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.devices.domain.model.aggregates.Sensor;
import com.jouletracker.platform.devices.domain.model.valueobjects.SerialNumber;
import com.jouletracker.platform.devices.domain.repositories.SensorRepository;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers.SensorPersistenceAssembler;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories.SensorPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SensorRepositoryImpl implements SensorRepository {

    private final SensorPersistenceRepository sensorPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SensorRepositoryImpl(SensorPersistenceRepository sensorPersistenceRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.sensorPersistenceRepository = sensorPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Sensor> findById(Long id) {
        return sensorPersistenceRepository.findById(id)
                .map(SensorPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Sensor> findBySerialNumber(SerialNumber serialNumber) {
        return sensorPersistenceRepository.findBySerialNumber(serialNumber.value())
                .map(SensorPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Sensor> findAllByUserId(Long userId) {
        return sensorPersistenceRepository.findAllByUserId(userId).stream()
                .map(SensorPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Sensor> findAll() {
        return sensorPersistenceRepository.findAll().stream()
                .map(SensorPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Sensor> findByAssignedDeviceId(Long deviceId) {
        return sensorPersistenceRepository.findByAssignedDeviceId(deviceId).stream()
                .map(SensorPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Sensor save(Sensor sensor) {
        boolean isNew = sensor.getId() == null;
        var entity = SensorPersistenceAssembler.toPersistenceFromDomain(sensor);
        var savedEntity = sensorPersistenceRepository.save(entity);
        var savedSensor = SensorPersistenceAssembler.toDomainFromPersistence(savedEntity);

        if (isNew) {
            savedSensor.onRegistered();
        }
        sensor.domainEvents().forEach(eventPublisher::publishEvent);
        sensor.clearDomainEvents();
        savedSensor.domainEvents().forEach(eventPublisher::publishEvent);
        savedSensor.clearDomainEvents();

        return savedSensor;
    }

    @Override
    public void deleteById(Long id) {
        sensorPersistenceRepository.deleteById(id);
    }

    @Override
    public boolean existsBySerialNumber(SerialNumber serialNumber) {
        return sensorPersistenceRepository.existsBySerialNumber(serialNumber.value());
    }
}
