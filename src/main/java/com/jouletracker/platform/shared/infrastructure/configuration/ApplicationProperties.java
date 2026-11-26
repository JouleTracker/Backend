package com.jouletracker.platform.shared.infrastructure.configuration;

import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Top-level application configuration properties for frontend integration and transactional links.
 *
 * <p>Validates {@code FRONTEND_BASE_URL} ({@code application.frontend-url}) at startup so
 * verification and password-reset links fail fast rather than emitting broken URLs.</p>
 */
@Validated
@NullMarked
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationProperties.class);

    private String frontendUrl = "http://localhost:4200";
    private @Nullable String emailVerificationUrl = "";
    private @Nullable String passwordResetUrl = "";

    /**
     * Validates configured frontend base URL at application startup.
     *
     * @throws IllegalStateException if {@code FRONTEND_BASE_URL} is missing or malformed
     */
    @PostConstruct
    public void validateConfiguration() {
        validateFrontendBaseUrl();
        LOGGER.info("Validated FRONTEND_BASE_URL configuration: {}", getNormalizedFrontendBaseUrl());
    }

    /**
     * Validates and returns the normalized {@code FRONTEND_BASE_URL} without any trailing slash.
     *
     * @return normalized frontend base URL
     * @throws IllegalStateException if the URL is blank or not a valid HTTP/HTTPS URI
     */
    public String getNormalizedFrontendBaseUrl() {
        return validateFrontendBaseUrl();
    }

    /**
     * Builds the full email verification link containing the single-use verification token.
     *
     * @param token the raw single-use verification token
     * @return the complete verification link
     */
    public String buildEmailVerificationUrl(String token) {
        String base = resolveEndpointUrl(emailVerificationUrl, "/verify-email");
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "token=" + token;
    }

    /**
     * Builds the full password reset link containing the single-use recovery token.
     *
     * @param token the raw single-use password reset token
     * @return the complete password reset link
     */
    public String buildPasswordResetUrl(String token) {
        String base = resolveEndpointUrl(passwordResetUrl, "/reset-password");
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "token=" + token;
    }

    private String resolveEndpointUrl(@Nullable String explicitOverride, String defaultPath) {
        String normalizedBase = validateFrontendBaseUrl();
        if (explicitOverride != null && !explicitOverride.isBlank()) {
            String trimmed = explicitOverride.trim();
            validateHttpUri(trimmed, "Configured transactional link URL");
            return trimmed;
        }
        return normalizedBase + defaultPath;
    }

    private String validateFrontendBaseUrl() {
        if (frontendUrl == null || frontendUrl.isBlank()) {
            LOGGER.error("Missing required FRONTEND_BASE_URL (application.frontend-url) configuration.");
            throw new IllegalStateException("FRONTEND_BASE_URL (application.frontend-url) must be configured and non-blank");
        }
        String trimmed = frontendUrl.trim();
        validateHttpUri(trimmed, "FRONTEND_BASE_URL");
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private void validateHttpUri(String candidate, String propertyName) {
        try {
            URI uri = new URI(candidate);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null || uri.getHost().isBlank()) {
                LOGGER.error("Invalid {} value '{}': must be a valid HTTP/HTTPS URL with a host.", propertyName, candidate);
                throw new IllegalStateException(propertyName + " must be a valid HTTP or HTTPS URL: " + candidate);
            }
        } catch (URISyntaxException ex) {
            LOGGER.error("Malformed {} URI '{}'.", propertyName, candidate);
            throw new IllegalStateException(propertyName + " is not a valid URI: " + candidate, ex);
        }
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String getEmailVerificationUrl() {
        return resolveEndpointUrl(emailVerificationUrl, "/verify-email");
    }

    public void setEmailVerificationUrl(@Nullable String emailVerificationUrl) {
        this.emailVerificationUrl = emailVerificationUrl;
    }

    public String getPasswordResetUrl() {
        return resolveEndpointUrl(passwordResetUrl, "/reset-password");
    }

    public void setPasswordResetUrl(@Nullable String passwordResetUrl) {
        this.passwordResetUrl = passwordResetUrl;
    }
}
