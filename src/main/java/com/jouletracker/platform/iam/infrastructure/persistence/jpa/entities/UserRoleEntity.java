package com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Intermediate JPA entity mapped to the {@code user_roles} join table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_roles_user_role", columnNames = {"user_id", "role_id"})
        }
)
public class UserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_roles_user")
    )
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_roles_role")
    )
    private RoleEntity role;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    public UserRoleEntity(Long id, UserEntity user, RoleEntity role, Instant assignedAt) {
        this.id = id;
        this.user = user;
        this.role = role;
        this.assignedAt = assignedAt != null ? assignedAt : Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleEntity that = (UserRoleEntity) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(user, that.user) && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(user, role);
    }
}
