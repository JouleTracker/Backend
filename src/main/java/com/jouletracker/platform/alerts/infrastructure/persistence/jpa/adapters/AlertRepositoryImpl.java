package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.adapters;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.repositories.AlertRepository;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers.AlertPersistenceAssembler;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.repositories.AlertPersistenceRepository;
import com.jouletracker.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter implementing {@link AlertRepository}.
 */
@Repository
public class AlertRepositoryImpl implements AlertRepository {

    private final AlertPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public AlertRepositoryImpl(AlertPersistenceRepository persistenceRepository,
                               DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Alert save(Alert alert) {
        var entity = AlertPersistenceAssembler.toPersistenceFromDomain(alert);
        var saved = persistenceRepository.save(entity);
        var domain = AlertPersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClearEvents(alert);
        return domain;
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return persistenceRepository.findById(id)
                .map(AlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Alert> findAllByUserIdOrderByCreatedAtDesc(Long userId) {
        return persistenceRepository.findAllByUserIdOrderByTriggeredAtDesc(userId).stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsActiveByUserIdAndSensorIdAndTitle(Long userId, Long sensorId, String title) {
        if (sensorId == null) {
            return persistenceRepository.existsByUserIdAndSensorIdIsNullAndTitleAndReadFalse(userId, title);
        }
        return persistenceRepository.existsByUserIdAndSensorIdAndTitleAndReadFalse(userId, sensorId, title);
    }
}
