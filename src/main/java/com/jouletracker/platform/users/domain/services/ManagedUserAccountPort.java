package com.jouletracker.platform.users.domain.services;

import com.jouletracker.platform.users.domain.model.valueobjects.AccountModerationStatus;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer (ACL) port isolating the User Management bounded context
 * from IAM persistence and cryptographic implementation details.
 */
@NullMarked
public interface ManagedUserAccountPort {

    /**
     * Retrieves a managed user snapshot by unique identifier.
     *
     * @param userId the user UUID
     * @return an Optional containing the user snapshot if found
     */
    Optional<ManagedUserSnapshot> findById(UUID userId);

    /**
     * Verifies whether a candidate plain-text password matches the stored hash for the given user.
     *
     * @param userId      the user UUID
     * @param rawPassword the candidate plain-text current password
     * @return true if the password matches the stored hash
     */
    boolean verifyPassword(UUID userId, String rawPassword);

    /**
     * Hashes and updates the user's password through IAM aggregate domain behavior.
     *
     * @param userId         the user UUID
     * @param newRawPassword the new plain-text password to hash and persist
     * @return updated user snapshot
     */
    ManagedUserSnapshot updatePassword(UUID userId, String newRawPassword);

    /**
     * Soft-deactivates the user account via IAM aggregate domain behavior.
     *
     * @param userId        the user UUID
     * @param deactivatedAt UTC timestamp of deactivation
     * @return updated user snapshot
     */
    ManagedUserSnapshot deactivateAccount(UUID userId, Instant deactivatedAt);

    /**
     * Updates the user account status via IAM aggregate domain behavior (admin moderation).
     *
     * @param userId       the target user UUID
     * @param targetStatus the new moderation status
     * @param timestamp    UTC timestamp of the moderation change
     * @return updated user snapshot
     */
    ManagedUserSnapshot updateAccountStatus(UUID userId, AccountModerationStatus targetStatus, Instant timestamp);
}
