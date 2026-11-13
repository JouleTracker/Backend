package com.jouletracker.platform.iam.infrastructure.email.brevo.client;

import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Request payload model for Brevo v3 Transactional Email API (POST /v3/smtp/email).
 *
 * @param sender      the sender name and email
 * @param to          the list of recipients
 * @param subject     the email subject
 * @param htmlContent the raw HTML email body
 */
@NullMarked
public record BrevoSendSmtpEmailRequest(
        Sender sender,
        List<Recipient> to,
        String subject,
        String htmlContent) {

    /**
     * Sender details.
     */
    public record Sender(String name, String email) {
    }

    /**
     * Recipient details.
     */
    public record Recipient(String email, String name) {
    }
}
