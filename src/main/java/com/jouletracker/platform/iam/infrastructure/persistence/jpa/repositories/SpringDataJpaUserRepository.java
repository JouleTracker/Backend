package com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link UserEntity}.
 */
@Repository
public interface SpringDataJpaUserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user entity by unique email address.
     *
     * @param email normalized email address
     * @return an Optional containing the UserEntity if found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks whether an account exists with the given email address.
     *
     * @param email normalized email address
     * @return true if an account exists, false otherwise
     */
    boolean existsByEmail(String email);
}
