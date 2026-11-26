package com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers;

import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Mapper bridging between {@link PasswordResetToken} domain entities and {@link PasswordResetTokenEntity}.
 *
 * <p>Stores SHA-256 digests rather than raw tokens at rest to mitigate database disclosure risks.</p>
 */
@Component
@NullMarked
public class PasswordResetTokenPersistenceMapper {

    /**
     * Converts a JPA entity into a domain {@link PasswordResetToken}.
     *
     * @param entity the JPA entity
     * @return the domain PasswordResetToken
     */
    public PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                entity.getId(),
                entity.getToken(),
                UserId.of(entity.getUserId()),
                entity.getExpiresAt(),
                entity.getUsedAt()
        );
    }

    /**
     * Converts a domain {@link PasswordResetToken} into a JPA entity, hashing the raw token string.
     *
     * @param domain the domain entity
     * @return a new PasswordResetTokenEntity ready for persistence
     */
    public PasswordResetTokenEntity toEntity(PasswordResetToken domain) {
        Long userIdValue = Objects.requireNonNull(
                domain.getUserId().value(),
                "UserId value must not be null when persisting a password reset token"
        );

        var entity = new PasswordResetTokenEntity(
                hash(domain.getToken()),
                userIdValue,
                domain.getExpiresAt(),
                domain.getUsedAt()
        );

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        return entity;
    }

    /**
     * Updates mutable fields of a managed JPA entity without re-hashing the stored token digest.
     *
     * @param entity the managed JPA entity
     * @param domain the domain token entity
     */
    public void updateEntity(PasswordResetTokenEntity entity, PasswordResetToken domain) {
        if (domain.getUserId().value() != null) {
            entity.setUserId(domain.getUserId().value());
        }
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setUsedAt(domain.getUsedAt());
    }

    /**
     * Computes the SHA-256 hex digest of a raw password reset token.
     *
     * @param token raw token string
     * @return 64-character lowercase hex digest
     */
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
