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
 * JPA persistence entity mapped to the PostgreSQL {@code password_reset_tokens} table.
 *
 * <p>Stores SHA-256 digests of single-use password recovery tokens along with expiration
 * and consumption timestamps.</p>
 */
@Getter
@Setter
@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_password_reset_tokens_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_password_reset_tokens_token", columnList = "token"),
                @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id")
        }
)
public class PasswordResetTokenEntity extends AuditableAbstractPersistenceEntity {

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

    public PasswordResetTokenEntity() {
    }

    public PasswordResetTokenEntity(String token, UUID userId, Instant expiresAt, Instant usedAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }
}
