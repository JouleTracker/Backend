package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a physical business area
 * persisted in the database.
 */
@Getter
@Setter
@Entity
@Table(name = "business_areas")
public class BusinessAreaPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    public BusinessAreaPersistenceEntity() {
    }
}
