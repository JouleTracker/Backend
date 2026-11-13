package com.jouletracker.platform.alerts.domain.model.aggregates;

import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertSeverity;
import com.jouletracker.platform.alerts.domain.model.valueobjects.AlertStatus;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root representing an automated energy consumption or power threshold alert (TS-14).
 */
@Getter
@Setter
public class Alert extends AbstractDomainAggregateRoot<Alert> {

    private Long id;
    private Long userId;
    private Long sensorId;
    private Long deviceId;
    private String title;
    private String description;
    private AlertSeverity severity;
    private String actionUrl;
    private boolean read;
    private AlertStatus status;
    private LocalDateTime createdAt;

    public Alert() {
        this.severity = AlertSeverity.WARNING;
        this.actionUrl = "/dispositivos";
        this.read = false;
        this.status = AlertStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public Alert(Long id, Long userId, Long sensorId, Long deviceId,
                 String title, String description, AlertSeverity severity,
                 String actionUrl, boolean read, AlertStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.sensorId = sensorId;
        this.deviceId = deviceId;
        this.title = Objects.requireNonNull(title, "title must not be null").trim();
        this.description = description != null ? description.trim() : "";
        this.severity = severity != null ? severity : AlertSeverity.WARNING;
        this.actionUrl = (actionUrl != null && !actionUrl.isBlank()) ? actionUrl.trim() : "/dispositivos";
        this.read = read;
        this.status = status != null ? status : (read ? AlertStatus.READ : AlertStatus.ACTIVE);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * Marks this alert as read/resolved by the user.
     */
    public void markAsRead() {
        this.read = true;
        this.status = AlertStatus.READ;
    }
}
