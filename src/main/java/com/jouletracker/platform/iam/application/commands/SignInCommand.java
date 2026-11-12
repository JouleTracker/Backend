package com.jouletracker.platform.iam.application.commands;

import org.jspecify.annotations.NullMarked;

/**
 * Command carrying credentials required to authenticate a user.
 *
 * @param email       the user's email address
 * @param rawPassword the plain-text password to verify
 */
@NullMarked
public record SignInCommand(
        String email,
        String rawPassword) {
}
