
package com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * US-48: JPA entity for persistent home configuration.
 */
@Getter
@Setter
@Entity
@Table(name = "home_configurations")
public class HomeConfigurationPersistenceEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "home_type", nullable = false, length = 50)
    private String homeType;

    @Column(name = "occupants", nullable = false)
    private Integer occupants;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "tariff", nullable = false, length = 100)
    private String tariff;

    public HomeConfigurationPersistenceEntity() {
    }
}
