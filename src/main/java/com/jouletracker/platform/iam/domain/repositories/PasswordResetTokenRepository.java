package com.jouletracker.platform.iam.domain.repositories;

import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Domain repository contract for managing {@link PasswordResetToken} persistence.
 *
 * <p>Free of JPA, Spring Data, and database-specific dependencies.</p>
 */
@NullMarked
public interface PasswordResetTokenRepository {

    /**
     * Finds a password reset token by its unique raw token string.
     *
     * @param token the raw cryptographic token string
     * @return an Optional containing the token entity if found, or empty otherwise
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Persists or updates the given password reset token entity.
     *
     * @param token the token entity to persist
     * @return the persisted PasswordResetToken
     */
    PasswordResetToken save(PasswordResetToken token);

    /**
     * Invalidates all existing password reset tokens belonging to the given user ID.
     *
     * @param userId the user ID whose tokens should be removed
     */
    void deleteByUserId(UserId userId);
}
