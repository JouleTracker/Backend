package com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.devices.domain.model.valueobjects.DeviceStatus;
import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing an electrical device persisted in the database.
 */
@Getter
@Setter
@Entity
@Table(name = "devices")
public class DevicePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeviceStatus status;

    @Column(name = "current_power_kw")
    private Double currentPowerKw;

    @Column(name = "power_limit_kw")
    private Double powerLimitKw;

    @Column(name = "maintenance_due_date", length = 50)
    private String maintenanceDueDate;

    @Column(name = "today_kwh")
    private Double todayKwh;

    @Column(name = "last_activity", length = 100)
    private String lastActivity;

    public DevicePersistenceEntity() {
    }
}
