package com.jouletracker.platform.iam.application.commands;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Command completing a password recovery flow with a single-use token and a new password.
 *
 * @param token       the raw single-use password reset token
 * @param newPassword the candidate new plain-text password
 */
@NullMarked
public record ResetPasswordCommand(String token, String newPassword) {
    public ResetPasswordCommand {
        Objects.requireNonNull(token, "Password reset token must not be null");
        Objects.requireNonNull(newPassword, "New password must not be null");
    }
}
