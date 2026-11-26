package com.jouletracker.platform.users.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Immutable domain snapshot representing a managed user account within the User Management bounded context.
 *
 * @param id            the unique user UUID
 * @param email         the user's normalized email address
 * @param firstName     the user's first name
 * @param lastName      the user's last name
 * @param roles         the assigned security roles
 * @param status        the current lifecycle/moderation status name
 * @param emailVerified whether the email address is confirmed
 * @param deactivatedAt UTC timestamp when the account was soft-deactivated, or null if active
 */
@NullMarked
public record ManagedUserSnapshot(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        String status,
        boolean emailVerified,
        @Nullable Instant deactivatedAt) {

    public boolean isDeactivated() {
        return "INACTIVE".equalsIgnoreCase(status);
    }

    public boolean isBanned() {
        return "BANNED".equalsIgnoreCase(status);
    }

    public boolean isRestricted() {
        return "BANNED".equalsIgnoreCase(status) || "SUSPENDED".equalsIgnoreCase(status);
    }
}
