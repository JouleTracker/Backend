package com.jouletracker.platform.iam.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Value object representing a securely hashed password.
 *
 * <p>Encapsulates the invariant that user passwords in the domain layer must always be stored
 * in hashed form, never in plain text.</p>
 *
 * @param value the encoded hash string
 */
@NullMarked
public record PasswordHash(String value) {

    /**
     * Canonical constructor validating that the hash string is non-empty.
     *
     * @param value the encoded hash string
     */
    public PasswordHash {
        Objects.requireNonNull(value, "Password hash must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
    }

    /**
     * Factory method creating a {@link PasswordHash}.
     *
     * @param value the encoded hash string
     * @return a new PasswordHash instance
     */
    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
