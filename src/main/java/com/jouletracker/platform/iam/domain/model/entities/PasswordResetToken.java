package com.jouletracker.platform.iam.domain.model.entities;

import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordResetTokenStatus;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a single-use, time-limited password recovery token.
 *
 * <p>Encapsulates token expiration, single-use consumption tracking, and lifecycle status.</p>
 */
@NullMarked
public class PasswordResetToken {

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
    public PasswordResetToken(
            @Nullable Long id,
            String token,
            UserId userId,
            Instant expiresAt,
            @Nullable Instant usedAt) {
        this.id = id;
        this.token = Objects.requireNonNull(token, "Password reset token string must not be null");
        this.userId = Objects.requireNonNull(userId, "UserId must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration instant must not be null");
        this.usedAt = usedAt;
    }

    /**
     * Factory method creating a new pending password recovery token for a user.
     *
     * @param userId           the associated user's identifier
     * @param token            the unique cryptographic token string
     * @param validityDuration how long the token remains valid
     * @return a new unpersisted PasswordResetToken instance
     */
    public static PasswordResetToken create(UserId userId, String token, Duration validityDuration) {
        Objects.requireNonNull(validityDuration, "Validity duration must not be null");
        return new PasswordResetToken(
                null,
                token,
                userId,
                Instant.now().plus(validityDuration),
                null
        );
    }

    /**
     * Checks whether the token has passed its expiration timestamp.
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
     * Validates whether this token can currently be consumed for a password reset.
     *
     * @return true if neither expired nor previously used
     */
    public boolean isValid() {
        return !isUsed() && !isExpired();
    }

    /**
     * Marks the password recovery token as consumed (single-use enforcement).
     *
     * @throws IllegalStateException if the token has already been used or has expired
     */
    public void markAsUsed() {
        if (isUsed()) {
            throw new IllegalStateException("Password reset token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Password reset token has expired");
        }
        this.usedAt = Instant.now();
    }

    /**
     * Computes the current lifecycle status of this password reset token.
     *
     * @return {@link PasswordResetTokenStatus}
     */
    public PasswordResetTokenStatus getStatus() {
        if (isUsed()) {
            return PasswordResetTokenStatus.USED;
        }
        if (isExpired()) {
            return PasswordResetTokenStatus.EXPIRED;
        }
        return PasswordResetTokenStatus.PENDING;
    }

    public @Nullable Long getId() {
        return id;
    }

    public void assignId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Cannot reassign an existing password reset token ID");
        }
        this.id = Objects.requireNonNull(id, "Token ID must not be null");
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
