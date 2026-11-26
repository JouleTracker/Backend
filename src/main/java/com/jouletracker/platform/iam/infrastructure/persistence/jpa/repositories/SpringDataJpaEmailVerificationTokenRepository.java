package com.jouletracker.platform.iam.infrastructure.persistence.jpa.repositories;

import com.jouletracker.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link EmailVerificationTokenEntity}.
 */
@Repository
public interface SpringDataJpaEmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    /**
     * Finds a verification token entity by its cryptographic hash string.
     *
     * @param token the hashed token string
     * @return an Optional containing the entity if found
     */
    Optional<EmailVerificationTokenEntity> findByToken(String token);

    /**
     * Deletes all tokens belonging to the given user ID.
     *
     * @param userId the user ID
     */
    @Modifying
    void deleteByUserId(Long userId);
}
