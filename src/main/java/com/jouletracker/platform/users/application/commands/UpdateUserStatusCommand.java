package com.jouletracker.platform.users.application.commands;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Administrative command updating a user account's moderation status (US-49).
 *
 * @param targetUserId the UUID of the user account being moderated
 * @param adminUserId  the UUID of the administrator performing the action
 * @param targetStatus the desired status string (e.g., ACTIVE, SUSPENDED, BANNED, INACTIVE)
 * @param reason       optional moderation reason for audit logging
 */
@NullMarked
public record UpdateUserStatusCommand(
        UUID targetUserId,
        UUID adminUserId,
        String targetStatus,
        @Nullable String reason) {
    public UpdateUserStatusCommand {
        Objects.requireNonNull(targetUserId, "Target user ID must not be null");
        Objects.requireNonNull(adminUserId, "Admin user ID must not be null");
        Objects.requireNonNull(targetStatus, "Target status must not be null");
    }
}
