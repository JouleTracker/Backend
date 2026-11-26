package com.jouletracker.platform.iam.application.commands;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Command initiating a password recovery flow for an existing user account.
 *
 * @param email the user's registered email address
 */
@NullMarked
public record ForgotPasswordCommand(String email) {
    public ForgotPasswordCommand {
        Objects.requireNonNull(email, "Email must not be null");
    }
}
