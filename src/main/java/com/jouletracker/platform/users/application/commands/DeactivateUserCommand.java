package com.jouletracker.platform.users.application.commands;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Command requesting soft-deactivation of a user account (US-40).
 *
 * @param targetUserId        the ID of the account to deactivate
 * @param authenticatedUserId the ID of the currently authenticated caller
 */
@NullMarked
public record DeactivateUserCommand(
        Long targetUserId,
        Long authenticatedUserId) {
    public DeactivateUserCommand {
        Objects.requireNonNull(targetUserId, "Target user ID must not be null");
        Objects.requireNonNull(authenticatedUserId, "Authenticated user ID must not be null");
    }
}
