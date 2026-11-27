
package com.jouletracker.platform.businessenergy.domain.model.commands;

import java.util.List;

/**
 * Command to update physical business area details
 * (US-31 Escenario 3).
 *
 * US-32: Supports updating shared notification recipients
 * associated with a business area.
 */
public record UpdateBusinessAreaCommand(
        Long id,
        Long userId,
        String name,
        String floor,
        String description,
        String icon,
        List<String> notificationRecipients
) {

    public UpdateBusinessAreaCommand {
        notificationRecipients = notificationRecipients != null
                ? List.copyOf(notificationRecipients)
                : null;
    }

    /**
     * Maintains compatibility with existing update commands.
     * Existing notification recipients remain unchanged.
     */
    public UpdateBusinessAreaCommand(
            Long id,
            Long userId,
            String name,
            String floor,
            String description,
            String icon) {

        this(
                id,
                userId,
                name,
                floor,
                description,
                icon,
                null
        );
    }
}
