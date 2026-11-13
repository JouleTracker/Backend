package com.jouletracker.platform.shared.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Top-level application configuration properties.
 */
@Validated
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private String frontendUrl = "http://localhost:4200";
    private String emailVerificationUrl = "http://localhost:4200/verify-email";

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String getEmailVerificationUrl() {
        return emailVerificationUrl;
    }

    public void setEmailVerificationUrl(String emailVerificationUrl) {
        this.emailVerificationUrl = emailVerificationUrl;
    }
}
