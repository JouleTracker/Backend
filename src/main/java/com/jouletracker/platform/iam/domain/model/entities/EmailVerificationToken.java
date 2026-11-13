package com.jouletracker.platform.iam.domain.model.entities;

import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.VerificationTokenStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a single-use email verification token.
 *
 * <p>Encapsulates token expiration, usage tracking, and lifecycle status.</p>
 */
@NullMarked
public class EmailVerificationToken {

    private @Nullable Long id;
    private final String token;
    private final UserId userId;
    private final Instant expiresAt;
    private @Nullable Instant usedAt;

    /**
     * Reconstitution constructor used by persistence mappers.
     *
     * @param id        the token identifier, or null if unpersisted
     * @param token     the unique cryptographic token string
     * @param userId    the identifier of the associated user
     * @param expiresAt the timestamp after which this token is invalid
     * @param usedAt    the timestamp when the token was consumed, or null if unused
     */
    public EmailVerificationToken(
            @Nullable Long id,
            String token,
            UserId userId,
            Instant expiresAt,
            @Nullable Instant usedAt) {
        this.id = id;
        this.token = Objects.requireNonNull(token, "Token string must not be null");
        this.userId = Objects.requireNonNull(userId, "UserId must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration instant must not be null");
        this.usedAt = usedAt;
    }

    /**
     * Factory method creating a new pending verification token for a user.
     *
     * @param userId           the associated user's ID
     * @param token            the unique token string
     * @param validityDuration how long the token remains valid
     * @return a new unpersisted EmailVerificationToken instance
     */
    public static EmailVerificationToken create(UserId userId, String token, Duration validityDuration) {
        return new EmailVerificationToken(
                null,
                token,
                userId,
                Instant.now().plus(validityDuration),
                null
        );
    }

    /**
     * Checks whether the token has passed its expiration time.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks whether the token has already been consumed.
     *
     * @return true if consumed, false otherwise
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Validates whether this token can currently be consumed for verification.
     *
     * @return true if neither expired nor previously used
     */
    public boolean isValid() {
        return !isUsed() && !isExpired();
    }

    /**
     * Marks the verification token as consumed.
     *
     * @throws IllegalStateException if the token has already been used or has expired
     */
    public void markAsUsed() {
        if (isUsed()) {
            throw new IllegalStateException("Verification token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Verification token has expired");
        }
        this.usedAt = Instant.now();
    }

    /**
     * Computes the current lifecycle status of this token.
     *
     * @return {@link VerificationTokenStatus}
     */
    public VerificationTokenStatus getStatus() {
        if (isUsed()) {
            return VerificationTokenStatus.USED;
        }
        if (isExpired()) {
            return VerificationTokenStatus.EXPIRED;
        }
        return VerificationTokenStatus.PENDING;
    }

    public @Nullable Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Cannot reassign an existing token ID");
        }
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public UserId getUserId() {
        return userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public @Nullable Instant getUsedAt() {
        return usedAt;
    }
}
