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
     * Suspended user account.
     */
    SUSPENDED,

    /**
     * Inactive or closed account.
     */
    INACTIVE
}
