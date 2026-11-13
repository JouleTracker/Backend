package com.jouletracker.platform.energy.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.energy.domain.model.aggregates.EnergyMeasurement;
import com.jouletracker.platform.energy.domain.repositories.EnergyMeasurementRepository;
import com.jouletracker.platform.energy.infrastructure.persistence.jpa.assemblers.EnergyMeasurementPersistenceAssembler;
import com.jouletracker.platform.energy.infrastructure.persistence.jpa.repositories.EnergyMeasurementPersistenceRepository;
import com.jouletracker.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EnergyMeasurementRepositoryImpl implements EnergyMeasurementRepository {

    private final EnergyMeasurementPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public EnergyMeasurementRepositoryImpl(EnergyMeasurementPersistenceRepository persistenceRepository,
                                           DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public EnergyMeasurement save(EnergyMeasurement measurement) {
        var entity = EnergyMeasurementPersistenceAssembler.toPersistenceFromDomain(measurement);
        var savedEntity = persistenceRepository.save(entity);
        var savedMeasurement = EnergyMeasurementPersistenceAssembler.toDomainFromPersistence(savedEntity);

        domainEventPublisher.publishAndClearEvents(measurement);

        return savedMeasurement;
    }

    @Override
    public Optional<EnergyMeasurement> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(EnergyMeasurementPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<EnergyMeasurement> findAllByUserId(Long userId) {
        return persistenceRepository.findAllByUserIdOrderByMeasuredAtDesc(userId).stream()
                .map(EnergyMeasurementPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<EnergyMeasurement> findAllByUserIdAndMeasuredAtBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return persistenceRepository.findAllByUserIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, start, end).stream()
                .map(EnergyMeasurementPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<EnergyMeasurement> findAllByUserIdAndDeviceIdAndMeasuredAtBetween(Long userId, Long deviceId, LocalDateTime start, LocalDateTime end) {
        return persistenceRepository.findAllByUserIdAndDeviceIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(userId, deviceId, start, end).stream()
                .map(EnergyMeasurementPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<EnergyMeasurement> findTopBySensorIdOrderByMeasuredAtDesc(Long sensorId) {
        return persistenceRepository.findTopBySensorIdOrderByMeasuredAtDesc(sensorId)
                .map(EnergyMeasurementPersistenceAssembler::toDomainFromPersistence);
    }
}