package com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the PostgreSQL {@code email_verification_tokens} table.
 *
 * <p>Extends {@link AuditableAbstractPersistenceEntity} to inherit auditing timestamps.
 * Uses PostgreSQL identity generation for the primary key and native {@code uuid} for {@code user_id}.</p>
 */
@Getter
@Setter
@Entity
@Table(
        name = "email_verification_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tokens_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_tokens_token", columnList = "token"),
                @Index(name = "idx_tokens_user_id", columnList = "user_id")
        }
)
public class EmailVerificationTokenEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    public EmailVerificationTokenEntity() {
    }

    public EmailVerificationTokenEntity(String token, UUID userId, Instant expiresAt, Instant usedAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }
}
