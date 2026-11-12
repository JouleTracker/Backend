package com.jouletracker.platform.iam.infrastructure.persistence.jpa.mappers;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.UserRoleEntity;
import com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories.SpringDataJpaRoleRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper bridging between the {@link User} domain aggregate root and the {@link UserEntity} JPA entity.
 */
@Component
@NullMarked
public class UserPersistenceMapper {

    private final SpringDataJpaRoleRepository roleRepository;

    public UserPersistenceMapper(SpringDataJpaRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Converts a JPA entity into a pure domain aggregate root.
     *
     * @param entity the JPA persistence entity
     * @return the domain User aggregate
     */
    public User toDomain(UserEntity entity) {
        Set<Role> roles = entity.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .map(RoleEntity::getName)
                .map(Role::fromDbName)
                .collect(Collectors.toSet());

        return new User(
                UserId.of(entity.getId()),
                EmailAddress.of(entity.getEmail()),
                PasswordHash.of(entity.getPasswordHash()),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getStatus(),
                entity.isEmailVerified(),
                roles
        );
    }

    /**
     * Converts a domain aggregate root into a new JPA entity for initial persistence.
     *
     * @param user the domain aggregate root
     * @return a new UserEntity instance
     */
    public UserEntity toEntity(User user) {
        UUID entityId = user.getId().value() != null ? user.getId().value() : UUID.randomUUID();

        var entity = new UserEntity(
                entityId,
                user.getEmail().value(),
                user.getPasswordHash().value(),
                user.getFirstName(),
                user.getLastName(),
                null,
                user.getStatus(),
                user.isEmailVerified()
        );

        assignRolesToEntity(entity, user.getRoles());
        return entity;
    }

    /**
     * Updates an existing managed JPA entity with the current state of the domain aggregate root.
     *
     * @param entity the managed JPA entity
     * @param user   the domain aggregate root containing updated state
     */
    public void updateEntity(UserEntity entity, User user) {
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash().value());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setStatus(user.getStatus());
        entity.setEmailVerified(user.isEmailVerified());

        Set<String> targetDbRoleNames = user.getRoles().stream()
                .map(Role::toDbName)
                .collect(Collectors.toSet());

        entity.getUserRoles().removeIf(ur -> !targetDbRoleNames.contains(ur.getRole().getName()));

        Set<String> existingDbRoleNames = entity.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());

        Set<Role> rolesToAdd = user.getRoles().stream()
                .filter(role -> !existingDbRoleNames.contains(role.toDbName()))
                .collect(Collectors.toSet());

        assignRolesToEntity(entity, rolesToAdd);
    }

    private void assignRolesToEntity(UserEntity entity, Set<Role> roles) {
        for (Role role : roles) {
            String dbRoleName = role.toDbName();
            RoleEntity roleEntity = roleRepository.findByName(dbRoleName)
                    .orElseGet(() -> {
                        var fallbackRole = new RoleEntity(UUID.randomUUID(), dbRoleName, "Role " + dbRoleName, null);
                        return roleRepository.save(fallbackRole);
                    });
            entity.addRole(roleEntity);
        }
    }
}