package com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.devices.domain.model.valueobjects.SensorNetworkStatus;
import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing an IoT sensor persisted in the database.
 */
@Getter
@Setter
@Entity
@Table(name = "sensors")
public class SensorPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SensorNetworkStatus status;

    @Column(name = "current_power_kw")
    private Double currentPowerKw;

    @Column(name = "today_kwh")
    private Double todayKwh;

    @Column(name = "assigned_device_id")
    private Long assignedDeviceId;

    @Column(name = "assigned_device_name", length = 100)
    private String assignedDeviceName;

    @Column(name = "assigned_device_category", length = 50)
    private String assignedDeviceCategory;

    @Column(name = "appliance_profile_id")
    private Long applianceProfileId;

    @Column(name = "appliance_profile_name", length = 100)
    private String applianceProfileName;

    @Column(name = "recommended_daily_kwh")
    private Double recommendedDailyKwh;

    @Column(name = "last_sync", length = 100)
    private String lastSync;

    public SensorPersistenceEntity() {
    }
}
