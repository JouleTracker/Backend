package com.jouletracker.platform.energy.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a persisted electrical energy measurement.
 */
@Getter
@Setter
@Entity
@Table(name = "energy_measurements")
public class EnergyMeasurementPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sensor_id")
    private Long sensorId;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_category", length = 50)
    private String deviceCategory;

    @Column(name = "power_kw", nullable = false)
    private Double powerKw;

    @Column(name = "energy_kwh", nullable = false)
    private Double energyKwh;

    @Column(name = "voltage")
    private Double voltage;

    @Column(name = "current_amps")
    private Double currentAmps;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    public EnergyMeasurementPersistenceEntity() {
    }
}