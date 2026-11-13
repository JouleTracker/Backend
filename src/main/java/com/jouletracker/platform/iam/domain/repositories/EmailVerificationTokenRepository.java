package com.jouletracker.platform.iam.domain.repositories;

import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Domain repository contract for managing {@link EmailVerificationToken} persistence.
 *
 * <p>Free of JPA, Spring Data, and database-specific dependencies.</p>
 */
@NullMarked
public interface EmailVerificationTokenRepository {

    /**
     * Finds an email verification token by its unique string.
     *
     * @param token the cryptographic token string
     * @return an Optional containing the token entity if found, or empty otherwise
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Persists or updates the given email verification token entity.
     *
     * @param token the token entity to persist
     * @return the persisted EmailVerificationToken
     */
    EmailVerificationToken save(EmailVerificationToken token);

    /**
     * Deletes all pending verification tokens associated with the given user ID.
     *
     * @param userId the user ID whose tokens should be invalidated
     */
    void deleteByUserId(UserId userId);
}
