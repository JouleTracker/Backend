package com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities;

import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code users} table.
 *
 * <p>Extends {@link AuditableAbstractPersistenceEntity} to inherit auditing fields.
 * Manages identity using UUID mapped to MySQL {@code CHAR(36)}.</p>
 */
@Getter
@Setter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
public class UserEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    public UserEntity() {
    }

    public UserEntity(
            UUID id,
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String phone,
            UserStatus status,
            boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = status != null ? status : UserStatus.PENDING_VERIFICATION;
        this.emailVerified = emailVerified;
    }

    public void addRole(RoleEntity role) {
        UserRoleEntity userRole = new UserRoleEntity(UUID.randomUUID(), this, role, Instant.now());
        this.userRoles.add(userRole);
    }

    public void removeRole(RoleEntity role) {
        this.userRoles.removeIf(ur -> ur.getRole().equals(role));
    }

    public void clearRoles() {
        this.userRoles.clear();
    }
}
