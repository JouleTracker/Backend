package com.jouletracker.platform.iam.infrastructure.email.brevo;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.infrastructure.configuration.VerificationTokenProperties;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoRestClient;
import com.jouletracker.platform.iam.infrastructure.email.brevo.client.BrevoSendSmtpEmailRequest;
import com.jouletracker.platform.shared.infrastructure.configuration.ApplicationProperties;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adapter implementing {@link EmailService} using Brevo's transactional email API.
 *
 * <p>Network or configuration failures are logged and handled gracefully so user
 * registration is not blocked when Brevo is unavailable or unconfigured (TS-19).</p>
 */
@Service
@NullMarked
public class BrevoEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrevoEmailService.class);

    private final BrevoRestClient brevoRestClient;
    private final BrevoProperties properties;
    private final ApplicationProperties applicationProperties;
    private final VerificationTokenProperties verificationTokenProperties;

    public BrevoEmailService(
            BrevoRestClient brevoRestClient,
            BrevoProperties properties,
            ApplicationProperties applicationProperties,
            VerificationTokenProperties verificationTokenProperties) {
        this.brevoRestClient = brevoRestClient;
        this.properties = properties;
        this.applicationProperties = applicationProperties;
        this.verificationTokenProperties = verificationTokenProperties;
    }

    @Override
    public void sendVerificationEmail(EmailAddress recipient, String recipientName, String verificationToken) {
        String verificationUrl = buildVerificationUrl(verificationToken);

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            LOGGER.warn("Brevo API key is not configured. Skipping email delivery. Verification URL for {}: {}",
                    recipient.value(), verificationUrl);
            return;
        }

        var sender = new BrevoSendSmtpEmailRequest.Sender(
                properties.getSenderName(),
                properties.getSenderEmail()
        );

        var to = List.of(new BrevoSendSmtpEmailRequest.Recipient(
                recipient.value(),
                recipientName
        ));

        String subject = "Confirm your JouleTracker account";
        String htmlContent = buildHtmlContent(recipientName, verificationUrl);

        var request = new BrevoSendSmtpEmailRequest(sender, to, subject, htmlContent);

        try {
            brevoRestClient.sendTransactionalEmail(request);
            LOGGER.info("Verification email accepted by Brevo.");
        } catch (Exception ex) {
            LOGGER.error("Failed to send verification email via Brevo for {}. Registration will proceed. Fallback verification URL: {}",
                    recipient.value(), verificationUrl, ex);
        }
    }

    private String buildVerificationUrl(String token) {
        String base = applicationProperties.getEmailVerificationUrl();
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "token=" + token;
    }

    private String buildHtmlContent(String recipientName, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>Confirm Your JouleTracker Account</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 24px; }
                        .container { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 32px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
                        .header { text-align: center; margin-bottom: 24px; }
                        .title { color: #1a202c; font-size: 22px; font-weight: 700; }
                        .content { color: #4a5568; line-height: 1.6; font-size: 15px; }
                        .button-container { text-align: center; margin: 32px 0; }
                        .button { background-color: #0284c7; color: #ffffff !important; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block; }
                        .footer { margin-top: 32px; font-size: 13px; color: #718096; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 16px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1 class="title">Welcome to JouleTracker!</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>Thank you for registering on JouleTracker Platform. To activate your account and start monitoring energy metrics, please verify your email address by clicking the button below:</p>
                            <div class="button-container">
                                <a href="%s" class="button" target="_blank">Verify Email Address</a>
                            </div>
                            <p>This verification link is single-use and will expire in <strong>%d hours</strong>.</p>
                            <p>If you did not create an account on JouleTracker, you can safely ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; %s JouleTracker Platform. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(recipientName, verificationUrl, verificationTokenProperties.getValidityHours(), String.valueOf(java.time.Year.now().getValue()));
    }
}
