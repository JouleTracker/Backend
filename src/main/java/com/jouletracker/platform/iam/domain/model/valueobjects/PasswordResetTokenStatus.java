package com.jouletracker.platform.iam.domain.model.valueobjects;

/**
 * Lifecycle status of a password recovery token.
 */
public enum PasswordResetTokenStatus {
    /**
     * Token has been generated and has not been used or expired yet.
     */
    PENDING,

    /**
     * Token was consumed to complete a password reset.
     */
    USED,

    /**
     * Token expired before being used.
     */
    EXPIRED
}
