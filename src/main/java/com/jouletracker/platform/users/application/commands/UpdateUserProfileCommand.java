package com.jouletracker.platform.users.application.commands;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Command requesting a personal profile update for an authenticated user (TS-12 / US-22).
 *
 * @param targetUserId        the ID of the user account to update
 * @param authenticatedUserId the ID of the currently authenticated caller
 * @param firstName           the updated first name
 * @param lastName            the updated last name
 * @param phone               optional contact phone number
 */
@NullMarked
public record UpdateUserProfileCommand(
        Long targetUserId,
        Long authenticatedUserId,
        String firstName,
        String lastName,
        @Nullable String phone) {
    public UpdateUserProfileCommand {
        Objects.requireNonNull(targetUserId, "Target user ID must not be null");
        Objects.requireNonNull(authenticatedUserId, "Authenticated user ID must not be null");
        Objects.requireNonNull(firstName, "First name must not be null");
        Objects.requireNonNull(lastName, "Last name must not be null");
    }
}
