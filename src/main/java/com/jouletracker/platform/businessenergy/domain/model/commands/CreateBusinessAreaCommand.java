
package com.jouletracker.platform.businessenergy.domain.model.commands;

import java.util.List;

/**
 * Command to register a physical business area
 * for energy management in JouleTracker.
 *
 * US-32: Supports shared notification recipients
 * during business area registration.
 */
public record CreateBusinessAreaCommand(
        Long userId,
        String name,
        String floor,
        String description,
        String icon,
        List<String> notificationRecipients
) {

    public CreateBusinessAreaCommand {
        notificationRecipients = notificationRecipients != null
                ? List.copyOf(notificationRecipients)
                : List.of();
    }

    /**
     * Maintains compatibility with existing area creation commands.
     */
    public CreateBusinessAreaCommand(
            Long userId,
            String name,
            String floor,
            String description,
            String icon) {

        this(
                userId,
                name,
                floor,
                description,
                icon,
                List.of()
        );
    }

    /**
     * Creates a business area using default floor and icon values.
     */
    public CreateBusinessAreaCommand(
            Long userId,
            String name,
            String description) {

        this(
                userId,
                name,
                "Piso 1",
                description,
                "storefront",
                List.of()
        );
    }
}
