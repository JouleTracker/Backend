package com.jouletracker.platform.iam.domain.services;

import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import org.jspecify.annotations.NullMarked;

/**
 * Domain service contract for cryptographic password hashing and verification.
 */
@NullMarked
public interface PasswordHashingService {

    /**
     * Hashes a raw plain-text password using a secure one-way function.
     *
     * @param rawPassword the plain-text password
     * @return the resulting {@link PasswordHash}
     */
    PasswordHash hash(String rawPassword);

    /**
     * Verifies whether a raw plain-text password matches an existing hash.
     *
     * @param rawPassword  the plain-text password to verify
     * @param passwordHash the stored password hash
     * @return true if the password matches the hash, false otherwise
     */
    boolean matches(String rawPassword, PasswordHash passwordHash);
}
