package com.jouletracker.platform.users.domain.services;

import com.jouletracker.platform.users.domain.model.valueobjects.AccountModerationStatus;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;
import java.util.Optional;

/**
 * Outbound Anti-Corruption Layer (ACL) port isolating the User Management bounded context
 * from IAM persistence and cryptographic implementation details.
 */
@NullMarked
public interface ManagedUserAccountPort {

    /**
     * Retrieves a managed user snapshot by unique identifier.
     *
     * @param userId the user ID
     * @return an Optional containing the user snapshot if found
     */
    Optional<ManagedUserSnapshot> findById(Long userId);

    /**
     * Verifies whether a candidate plain-text password matches the stored hash for the given user.
     *
     * @param userId      the user ID
     * @param rawPassword the candidate plain-text current password
     * @return true if the password matches the stored hash
     */
    boolean verifyPassword(Long userId, String rawPassword);

    /**
     * Hashes and updates the user's password through IAM aggregate domain behavior.
     *
     * @param userId         the user ID
     * @param newRawPassword the new plain-text password to hash and persist
     * @return updated user snapshot
     */
    ManagedUserSnapshot updatePassword(Long userId, String newRawPassword);

    /**
     * Soft-deactivates the user account via IAM aggregate domain behavior.
     *
     * @param userId        the user ID
     * @param deactivatedAt UTC timestamp of deactivation
     * @return updated user snapshot
     */
    ManagedUserSnapshot deactivateAccount(Long userId, Instant deactivatedAt);

    /**
     * Updates the user account status via IAM aggregate domain behavior (admin moderation).
     *
     * @param userId       the target user ID
     * @param targetStatus the new moderation status
     * @param timestamp    UTC timestamp of the moderation change
     * @return updated user snapshot
     */
    ManagedUserSnapshot updateAccountStatus(Long userId, AccountModerationStatus targetStatus, Instant timestamp);
}
