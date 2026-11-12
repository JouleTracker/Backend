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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the database table storing single-use email verification tokens.
 *
 * <p>Extends {@link AuditableAbstractPersistenceEntity} to inherit auditing fields.
 * Preserves the autoincremental {@code Long} identifier for token primary key while referencing
 * the user by {@code UUID} mapped to {@code CHAR(36)}.</p>
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

    @Column(nullable = false, length = 64)
    private String token;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, nullable = false)
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
