package com.jouletracker.platform.iam.domain.model.valueobjects;

/**
 * Lifecycle status of an email confirmation token.
 */
public enum VerificationTokenStatus {
    /**
     * The token has been generated and is awaiting verification by the recipient.
     */
    PENDING,

    /**
     * The token was successfully used to confirm the user's email address.
     */
    USED,

    /**
     * The token expired prior to being used.
     */
    EXPIRED
}
