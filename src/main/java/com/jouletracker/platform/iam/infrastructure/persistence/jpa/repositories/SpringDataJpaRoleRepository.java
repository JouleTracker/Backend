package com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RoleEntity}.
 */
@Repository
public interface SpringDataJpaRoleRepository extends JpaRepository<RoleEntity, Long> {

    /**
     * Finds a role entity by its unique name (e.g., "USER", "ADMIN").
     *
     * @param name the role name
     * @return an Optional containing the RoleEntity if found
     */
    Optional<RoleEntity> findByName(String name);
}
