package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a persisted energy consumption alert.
 */
@Getter
@Setter
@Entity
@Table(name = "alerts")
public class AlertPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sensor_id")
    private Long sensorId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "action_url", length = 120)
    private String actionUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    public AlertPersistenceEntity() {
    }
}
