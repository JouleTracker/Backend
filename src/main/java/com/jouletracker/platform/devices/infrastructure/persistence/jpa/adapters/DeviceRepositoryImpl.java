package com.jouletracker.platform.devices.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.devices.domain.model.aggregates.Device;
import com.jouletracker.platform.devices.domain.repositories.DeviceRepository;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.assemblers.DevicePersistenceAssembler;
import com.jouletracker.platform.devices.infrastructure.persistence.jpa.repositories.DevicePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DevicePersistenceRepository devicePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DeviceRepositoryImpl(DevicePersistenceRepository devicePersistenceRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.devicePersistenceRepository = devicePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Device> findById(Long id) {
        return devicePersistenceRepository.findById(id)
                .map(DevicePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Device> findAllByUserId(Long userId) {
        return devicePersistenceRepository.findAllByUserId(userId).stream()
                .map(DevicePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Device> findAll() {
        return devicePersistenceRepository.findAll().stream()
                .map(DevicePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Device save(Device device) {
        var entity = DevicePersistenceAssembler.toPersistenceFromDomain(device);
        var savedEntity = devicePersistenceRepository.save(entity);
        var savedDevice = DevicePersistenceAssembler.toDomainFromPersistence(savedEntity);

        // Publish domain events
        device.domainEvents().forEach(eventPublisher::publishEvent);
        device.clearDomainEvents();

        return savedDevice;
    }

    @Override
    public void deleteById(Long id) {
        devicePersistenceRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserIdAndName(Long userId, String name) {
        return devicePersistenceRepository.existsByUserIdAndName(userId, name);
    }
}
