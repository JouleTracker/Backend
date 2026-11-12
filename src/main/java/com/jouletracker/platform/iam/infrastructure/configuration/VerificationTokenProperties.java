package com.jouletracker.platform.iam.infrastructure.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for email verification tokens.
 */
@Validated
@ConfigurationProperties(prefix = "iam.verification-token")
public class VerificationTokenProperties {

    @Min(value = 1, message = "Token validity hours must be at least 1")
    private int validityHours = 24;

    public int getValidityHours() {
        return validityHours;
    }

    public void setValidityHours(int validityHours) {
        this.validityHours = validityHours;
    }

    public Duration getValidityDuration() {
        return Duration.ofHours(validityHours);
    }
}
