package com.jouletracker.platform.businessenergy.domain.model.commands;

/**
 * Command to register a physical business area
 * for energy management in JouleTracker.
 */
public record CreateBusinessAreaCommand(
        Long userId,
        String name,
        String description
) {
}
