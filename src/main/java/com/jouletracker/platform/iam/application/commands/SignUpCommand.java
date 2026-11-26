package com.jouletracker.platform.iam.application.commands;

import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Command carrying validated data required to register a new user account.
 *
 * @param email       the user's email address
 * @param rawPassword the plain-text password to be hashed
 * @param firstName   the user's first name
 * @param lastName    the user's optional last name (normalized to empty string when null/blank)
 * @param roles       the security roles to assign (defaults to ROLE_USER if empty)
 */
@NullMarked
public record SignUpCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        Set<Role> roles) {

    public SignUpCommand {
        lastName = lastName == null ? "" : lastName.trim();
    }

    public SignUpCommand(String email, String rawPassword, String firstName, @Nullable String lastName) {
        this(email, rawPassword, firstName, lastName == null ? "" : lastName.trim(), Set.of(Role.ROLE_USER));
    }
}
