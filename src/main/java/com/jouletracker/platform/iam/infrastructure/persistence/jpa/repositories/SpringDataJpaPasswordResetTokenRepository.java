package com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PasswordResetTokenEntity}.
 */
@Repository
public interface SpringDataJpaPasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Finds a password reset token entity by its SHA-256 hash digest.
     *
     * @param token the hashed token digest
     * @return an Optional containing the entity if found
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Deletes all password reset tokens belonging to the given user ID.
     *
     * @param userId the user ID
     */
    @Modifying
    void deleteByUserId(Long userId);
}
