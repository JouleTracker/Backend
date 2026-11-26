package com.jouletracker.platform.iam.application.commands;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Command requesting a fresh account verification email for an unverified user account.
 *
 * @param email the user's email address
 */
@NullMarked
public record ResendVerificationCommand(String email) {
    public ResendVerificationCommand {
        Objects.requireNonNull(email, "Email must not be null");
    }
}
