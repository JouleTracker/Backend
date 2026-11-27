
package com.jouletracker.platform.alerts.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * US-38: JPA persistence entity for user
 * notification preferences.
 */
@Getter
@Setter
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferencesPersistenceEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "whatsapp_enabled", nullable = false)
    private boolean whatsappEnabled;

    public NotificationPreferencesPersistenceEntity() {
    }
}
