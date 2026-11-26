package com.jouletracker.platform.businessenergy.domain.model.commands;

/**
 * Command to update physical business area details (US-31 Escenario 3).
 */
public record UpdateBusinessAreaCommand(
        Long id,
        Long userId,
        String name,
        String floor,
        String description,
        String icon
) {
}
