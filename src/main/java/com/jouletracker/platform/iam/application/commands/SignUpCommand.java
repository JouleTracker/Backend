package com.jouletracker.platform.iam.application.commands;

import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

/**
 * Command carrying validated data required to register a new user account.
 *
 * @param email       the user's email address
 * @param rawPassword the plain-text password to be hashed
 * @param firstName   the user's first name
 * @param lastName    the user's last name
 * @param roles       the security roles to assign (defaults to ROLE_USER if empty)
 */
@NullMarked
public record SignUpCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        Set<Role> roles) {

    public SignUpCommand(String email, String rawPassword, String firstName, String lastName) {
        this(email, rawPassword, firstName, lastName, Set.of(Role.ROLE_USER));
    }
}
