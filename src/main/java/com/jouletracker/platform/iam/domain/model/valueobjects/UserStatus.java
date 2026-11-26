package com.jouletracker.platform.iam.domain.model.valueobjects;

/**
 * Enumeration representing the account lifecycle status of a user.
 */
public enum UserStatus {
    /**
     * Account created, awaiting email confirmation.
     */
    PENDING_VERIFICATION,

    /**
     * Active user account with confirmed email.
     */
    ACTIVE,

    /**
     * Temporarily suspended user account blocked by moderation.
     */
    SUSPENDED,

    /**
     * Permanently banned user account blocked from accessing the platform.
     */
    BANNED,

    /**
     * Soft-deactivated or closed user account.
     */
    INACTIVE
}
