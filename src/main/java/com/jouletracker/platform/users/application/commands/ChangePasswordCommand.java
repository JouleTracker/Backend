package com.jouletracker.platform.users.application.commands;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.UUID;

/**
 * Command requesting a password update for an authenticated user (US-25).
 *
 * @param targetUserId        the UUID of the account whose password will be changed
 * @param authenticatedUserId the UUID of the currently authenticated caller
 * @param currentPassword     the caller's current plain-text password for verification
 * @param newPassword         the new plain-text password
 */
@NullMarked
public record ChangePasswordCommand(
        UUID targetUserId,
        UUID authenticatedUserId,
        String currentPassword,
        String newPassword) {
    public ChangePasswordCommand {
        Objects.requireNonNull(targetUserId, "Target user ID must not be null");
        Objects.requireNonNull(authenticatedUserId, "Authenticated user ID must not be null");
        Objects.requireNonNull(currentPassword, "Current password must not be null");
        Objects.requireNonNull(newPassword, "New password must not be null");
    }
}
