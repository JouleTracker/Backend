package com.jouletracker.platform.iam.application.commands;

import org.jspecify.annotations.NullMarked;

/**
 * Command carrying the single-use token required to verify an account email.
 *
 * @param token the cryptographic token string
 */
@NullMarked
public record VerifyEmailCommand(String token) {
}
