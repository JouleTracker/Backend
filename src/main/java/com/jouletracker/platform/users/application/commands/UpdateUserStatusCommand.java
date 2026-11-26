package com.jouletracker.platform.users.application.commands;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Administrative command updating a user account's moderation status (US-49).
 *
 * @param targetUserId the ID of the user account being moderated
 * @param adminUserId  the ID of the administrator performing the action
 * @param targetStatus the desired status string (e.g., ACTIVE, SUSPENDED, BANNED, INACTIVE)
 * @param reason       optional moderation reason for audit logging
 */
@NullMarked
public record UpdateUserStatusCommand(
        Long targetUserId,
        Long adminUserId,
        String targetStatus,
        @Nullable String reason) {
    public UpdateUserStatusCommand {
        Objects.requireNonNull(targetUserId, "Target user ID must not be null");
        Objects.requireNonNull(adminUserId, "Admin user ID must not be null");
        Objects.requireNonNull(targetStatus, "Target status must not be null");
    }
}
