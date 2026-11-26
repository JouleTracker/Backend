package com.jouletracker.platform.businessenergy.domain.model.commands;

/**
 * Command to register a physical business area
 * for energy management in JouleTracker.
 */
public record CreateBusinessAreaCommand(
        Long userId,
        String name,
        String floor,
        String description,
        String icon
) {
    public CreateBusinessAreaCommand(Long userId, String name, String description) {
        this(userId, name, "Piso 1", description, "storefront");
    }
}
