package com.jouletracker.platform.businessenergy.domain.model.commands;

/**
 * Command to delete a physical business area.
 */
public record DeleteBusinessAreaCommand(
        Long id,
        Long userId
) {
}
