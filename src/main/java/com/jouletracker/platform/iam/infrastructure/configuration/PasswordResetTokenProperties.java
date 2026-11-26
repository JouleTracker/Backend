package com.jouletracker.platform.iam.infrastructure.configuration;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties controlling the lifetime of password recovery tokens.
 */
@Validated
@ConfigurationProperties(prefix = "iam.password-reset-token")
public class PasswordResetTokenProperties {

    @Min(value = 1, message = "Password reset token validity minutes must be at least 1")
    private int validityMinutes = 30;

    public int getValidityMinutes() {
        return validityMinutes;
    }

    public void setValidityMinutes(int validityMinutes) {
        this.validityMinutes = validityMinutes;
    }

    public Duration getValidityDuration() {
        return Duration.ofMinutes(validityMinutes);
    }
}
