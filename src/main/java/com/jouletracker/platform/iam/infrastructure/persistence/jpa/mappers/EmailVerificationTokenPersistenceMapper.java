package com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers;

import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationTokenEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Mapper bridging between the {@link EmailVerificationToken} domain entity and {@link EmailVerificationTokenEntity}.
 */
@Component
@NullMarked
public class EmailVerificationTokenPersistenceMapper {

    /**
     * Converts a JPA token entity into a pure domain token entity.
     * Note: The entity stores the cryptographic hash of the token.
     *
     * @param entity the JPA entity
     * @return the domain EmailVerificationToken entity
     */
    public EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {
        return new EmailVerificationToken(
                entity.getId(),
                entity.getToken(),
                UserId.of(entity.getUserId()),
                entity.getExpiresAt(),
                entity.getUsedAt()
        );
    }

    /**
     * Converts a domain token entity into a JPA entity for persistence.
     * Hashes the raw token before persisting.
     *
     * @param domain the domain entity
     * @return a new EmailVerificationTokenEntity
     */
    public EmailVerificationTokenEntity toEntity(EmailVerificationToken domain) {
        Long userIdValue = Objects.requireNonNull(
                domain.getUserId().value(),
                "UserId value must not be null when persisting a verification token"
        );

        var entity = new EmailVerificationTokenEntity(
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
     * Updates an existing managed JPA token entity from domain entity state.
     *
     * <p>CRITICAL: Does NOT re-hash or modify the token string column to avoid
     * corrupting the database with double hashes upon entity update.</p>
     *
     * @param entity the managed JPA entity
     * @param domain the domain token entity
     */
    public void updateEntity(EmailVerificationTokenEntity entity, EmailVerificationToken domain) {
        if (domain.getUserId().value() != null) {
            entity.setUserId(domain.getUserId().value());
        }
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setUsedAt(domain.getUsedAt());
    }

    /**
     * Computes the SHA-256 hex digest for a token string.
     *
     * @param token raw token string
     * @return 64-character hex digest
     */
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
