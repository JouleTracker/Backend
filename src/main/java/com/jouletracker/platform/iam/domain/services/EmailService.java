
package com.jouletracker.platform.iam.domain.services;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import org.jspecify.annotations.NullMarked;

/**
 * Domain service contract for transactional email delivery.
 */
@NullMarked
public interface EmailService {

    /**
     * Sends an account confirmation email containing
     * a single-use verification token link.
     *
     * @param recipient the recipient email address
     * @param recipientName the full name of the recipient
     * @param verificationToken the unique verification token
     */
    void sendVerificationEmail(
            EmailAddress recipient,
            String recipientName,
            String verificationToken
    );

    /**
     * Sends a password recovery email containing
     * a single-use, time-limited reset token link.
     *
     * @param recipient the recipient email address
     * @param recipientName the full name of the recipient
     * @param resetToken the unique password recovery token
     */
    void sendPasswordResetEmail(
            EmailAddress recipient,
            String recipientName,
            String resetToken
    );

    /**
     * TS-19: Sends a critical energy consumption alert
     * to the owner or an authorized business area recipient.
     *
     * @param recipient the recipient email address
     * @param recipientName the recipient name
     * @param alertTitle the critical alert title
     * @param alertDescription the critical alert description
     * @param sensorName the sensor associated with the alert
     * @param businessAreaName the associated business area
     */
    void sendCriticalAlertEmail(
            EmailAddress recipient,
            String recipientName,
            String alertTitle,
            String alertDescription,
            String sensorName,
            String businessAreaName
    );
}
