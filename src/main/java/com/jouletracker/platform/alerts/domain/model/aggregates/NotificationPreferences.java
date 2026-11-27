
package com.jouletracker.platform.alerts.domain.model.aggregates;

import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * US-38: Aggregate root representing notification
 * channel preferences for an individual user.
 */
@Getter
public class NotificationPreferences
        extends AbstractDomainAggregateRoot<NotificationPreferences> {

    private final Long userId;

    private boolean emailEnabled;

    private boolean whatsappEnabled;

    public NotificationPreferences(
            Long userId,
            boolean emailEnabled,
            boolean whatsappEnabled) {

        if (userId == null || userId <= 0) {

            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }

        this.userId = userId;
        this.emailEnabled = emailEnabled;
        this.whatsappEnabled = whatsappEnabled;
    }

    /**
     * Creates default notification preferences.
     */
    public NotificationPreferences(Long userId) {

        this(userId, false, false);
    }

    /**
     * Updates the notification channel preferences.
     */
    public void updatePreferences(
            boolean emailEnabled,
            boolean whatsappEnabled) {

        this.emailEnabled = emailEnabled;
        this.whatsappEnabled = whatsappEnabled;
    }

    public void enableEmail() {

        this.emailEnabled = true;
    }

    public void disableEmail() {

        this.emailEnabled = false;
    }

    public void enableWhatsapp() {

        this.whatsappEnabled = true;
    }

    public void disableWhatsapp() {

        this.whatsappEnabled = false;
    }

    public boolean hasEnabledChannels() {

        return emailEnabled || whatsappEnabled;
    }
}
