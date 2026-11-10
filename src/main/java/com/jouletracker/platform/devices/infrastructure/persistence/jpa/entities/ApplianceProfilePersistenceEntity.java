package com.jouletracker.platform.devices.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for appliance consumption reference benchmarks.
 */
@Getter
@Setter
@Entity
@Table(name = "appliance_profiles")
public class ApplianceProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "recommended_daily_kwh")
    private Double recommendedDailyKwh;

    @Column(name = "typical_power_watts", length = 50)
    private String typicalPowerWatts;

    @Column(name = "icon", length = 50)
    private String icon;

    public ApplianceProfilePersistenceEntity() {
    }
}
