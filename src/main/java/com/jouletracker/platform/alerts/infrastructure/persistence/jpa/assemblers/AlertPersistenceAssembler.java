package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.alerts.domain.model.aggregates.Alert;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;

import java.time.LocalDateTime;

/**
 * Mapper converting between Alert domain aggregate and JPA persistence entity.
 */
public final class AlertPersistenceAssembler {

    private AlertPersistenceAssembler() {
    }

    public static Alert toDomainFromPersistence(AlertPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Alert(
                entity.getId(),
                entity.getUserId(),
                entity.getSensorId(),
                entity.getDeviceId(),
                entity.getTitle(),
                entity.getDescription(),
                AlertSeverity.fromString(entity.getSeverity()),
                entity.getActionUrl(),
                entity.isRead(),
                AlertStatus.fromString(entity.getStatus()),
                entity.getTriggeredAt()
        );
    }

    public static AlertPersistenceEntity toPersistenceFromDomain(Alert domain) {
        if (domain == null) {
            return null;
        }
        var entity = new AlertPersistenceEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setSensorId(domain.getSensorId());
        entity.setDeviceId(domain.getDeviceId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setSeverity(domain.getSeverity() != null ? domain.getSeverity().getCode() : AlertSeverity.WARNING.getCode());
        entity.setActionUrl(domain.getActionUrl());
        entity.setRead(domain.isRead());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().getDisplayName() : AlertStatus.ACTIVE.getDisplayName());
        entity.setTriggeredAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : LocalDateTime.now());
        return entity;
    }
}
