package com.jouletracker.platform.iam.interfaces.acl;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

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
        return userRepository.findById(UserId.of(userId)).isPresent();
    }

    /**
     * Fetches a user's ID by their email address.
     *
     * @param email the user's email address
     * @return an Optional containing the user ID if found
     */
    public Optional<Long> fetchUserIdByEmail(String email) {
        try {
            return userRepository.findByEmail(EmailAddress.of(email))
                    .map(user -> user.getId().value());
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Checks whether a user possesses the specified security role.
     *
     * @param userId the user ID
     * @param role   the role to check
     * @return true if the user exists and holds the role
     */
    public boolean userHasRole(Long userId, Role role) {
        return userRepository.findById(UserId.of(userId))
                .map(user -> user.getRoles().contains(role))
                .orElse(false);
    }
}
