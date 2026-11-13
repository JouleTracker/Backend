package com.jouletracker.platform.iam.domain.services;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import org.jspecify.annotations.NullMarked;

/**
 * Domain service contract for transactional email delivery.
 */
@NullMarked
public interface EmailService {

    /**
     * Sends an account confirmation email containing a verification token link.
     *
     * @param recipient         the recipient email address
     * @param recipientName     the full name of the recipient
     * @param verificationToken the unique single-use token string
     */
    void sendVerificationEmail(EmailAddress recipient, String recipientName, String verificationToken);
}
