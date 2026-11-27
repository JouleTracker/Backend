
package com.jouletracker.platform.alerts.interfaces.rest.transform;

import com.jouletracker.platform.alerts.domain.model.aggregates.NotificationPreferences;
import com.jouletracker.platform.alerts.interfaces.rest.resources.NotificationPreferencesResource;

/**
 * US-38: Converts domain notification preferences
 * into REST response resources.
 */
public final class NotificationPreferencesResourceFromEntityAssembler {

    private NotificationPreferencesResourceFromEntityAssembler() {
    }

    public static NotificationPreferencesResource toResourceFromEntity(
            NotificationPreferences entity) {

        return new NotificationPreferencesResource(
                entity.getUserId(),
                entity.isEmailEnabled(),
                entity.isWhatsappEnabled()
        );
    }
}
