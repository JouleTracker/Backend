
package com.jouletracker.platform.businessenergy.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JPA entity representing a physical business area
 * persisted in the database.
 *
 * US-32: Supports shared notification recipients
 * associated with each business area.
 */
@Getter
@Setter
@Entity
@Table(name = "business_areas")
public class BusinessAreaPersistenceEntity
        extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "floor", length = 50)
    private String floor;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * US-32: Shared notification recipients.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "business_area_notification_recipients",
            joinColumns = @JoinColumn(
                    name = "business_area_id",
                    nullable = false
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_business_area_recipient_email",
                    columnNames = {
                            "business_area_id",
                            "email"
                    }
            )
    )
    @Column(
            name = "email",
            nullable = false,
            length = 254
    )
    private Set<String> notificationRecipients =
            new LinkedHashSet<>();

    public BusinessAreaPersistenceEntity() {
    }
}
