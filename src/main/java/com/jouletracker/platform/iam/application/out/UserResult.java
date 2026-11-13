package com.jouletracker.platform.iam.application.out;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Application output record representing a safe, non-sensitive projection of a {@link User}.
 *
 * @param id            the unique user ID
 * @param email         the email address
 * @param firstName     the first name
 * @param lastName      the last name
 * @param roles         the set of assigned roles
 * @param emailVerified whether the email is confirmed
 */
@NullMarked
public record UserResult(
        @Nullable Long id,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        boolean emailVerified) {

    /**
     * Factory method mapping a domain {@link User} aggregate into a {@link UserResult}.
     *
     * @param user the domain aggregate root
     * @return the projected UserResult
     */
    public static UserResult fromUser(User user) {
        return new UserResult(
                user.getId().value(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.isEmailVerified()
        );
    }
}
