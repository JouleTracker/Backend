package com.jouletracker.platform.users.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;

import java.util.Locale;

/**
 * Moderation and lifecycle statuses assignable within the User Management bounded context.
 */
@NullMarked
public enum AccountModerationStatus {
    ACTIVE,
    SUSPENDED,
    BANNED,
    INACTIVE;

    /**
     * Parses a case-insensitive status string into an {@link AccountModerationStatus}.
     *
     * @param rawStatus the candidate status value
     * @return the corresponding enum constant
     * @throws IllegalArgumentException if the string does not match an allowed status
     */
    public static AccountModerationStatus fromString(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalArgumentException("El estado no puede estar vacío. Valores permitidos: ACTIVE, SUSPENDED, BANNED, INACTIVE");
        }
        try {
            return AccountModerationStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Estado de usuario inválido '" + rawStatus + "'. Valores permitidos: ACTIVE, SUSPENDED, BANNED, INACTIVE"
            );
        }
    }
}
