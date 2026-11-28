
package com.jouletracker.platform.iam.interfaces.acl;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Anti-Corruption Layer (ACL) facade exposing IAM user lookup capabilities
 * to other bounded contexts without coupling them to internal IAM repositories.
 */
@Service
@NullMarked
public class IamContextFacade {

    private final UserRepository userRepository;

    public IamContextFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Checks whether a user with the given ID exists.
     *
     * @param userId the user ID
     * @return true if the user exists
     */
    public boolean existsUserById(Long userId) {

        return userRepository
                .findById(UserId.of(userId))
                .isPresent();
    }

    /**
     * Fetches a user's ID by their email address.
     *
     * @param email the user's email address
     * @return an Optional containing the user ID if found
     */
    public Optional<Long> fetchUserIdByEmail(String email) {

        try {

            return userRepository
                    .findByEmail(EmailAddress.of(email))
                    .map(user -> user.getId().value());

        } catch (IllegalArgumentException ex) {

            return Optional.empty();
        }
    }

    /**
     * Checks whether a user possesses the specified security role.
     *
     * @param userId the user ID
     * @param role the role to check
     * @return true if the user exists and holds the role
     */
    public boolean userHasRole(Long userId, Role role) {

        return userRepository
                .findById(UserId.of(userId))
                .map(user -> user.getRoles().contains(role))
                .orElse(false);
    }

    /**
     * Retrieves IDs of all active non-administrator user accounts eligible for broadcast notifications.
     *
     * @return list of active non-admin user IDs
     */
    public List<Long> fetchActiveNonAdminUserIds() {
        return userRepository.findAll().stream()
                .filter(user -> user.canAuthenticate() && !user.getRoles().contains(Role.ROLE_ADMIN))
                .map(user -> user.getId().value())
                .toList();
    }

    /**
     * TS-19: Retrieves contact information for critical
     * energy alert notifications.
     *
     * Only accounts eligible for authentication
     * are returned.
     *
     * @param userId the user identifier
     * @return notification recipient information if available
     */
    public Optional<NotificationRecipient> fetchNotificationRecipient(
            @Nullable Long userId) {

        if (userId == null || userId <= 0) {
            return Optional.empty();
        }

        return userRepository
                .findById(UserId.of(userId))
                .filter(user -> user.canAuthenticate())
                .map(user -> new NotificationRecipient(
                        user.getId().value(),
                        user.getEmail().value(),
                        user.getFullName()
                ));
    }

    /**
     * TS-19: Read-only contact information exposed
     * to other bounded contexts.
     */
    public record NotificationRecipient(
            Long userId,
            String email,
            String fullName
    ) {
    }
}
