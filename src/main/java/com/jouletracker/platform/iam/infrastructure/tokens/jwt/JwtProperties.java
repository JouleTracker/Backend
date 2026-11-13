package com.jouletracker.platform.iam.infrastructure.tokens.jwt;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

/**
 * Configuration properties for JSON Web Token (JWT) generation and validation.
 */
@Validated
@ConfigurationProperties(prefix = "authorization.jwt")
public class JwtProperties {

    @NotBlank(message = "authorization.jwt.secret must not be blank")
    private String secret;

    @Min(value = 1, message = "authorization.jwt.expiration.days must be greater than zero")
    private int expirationDays = 7;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(int expirationDays) {
        this.expirationDays = expirationDays;
    }

    @PostConstruct
    void validate() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("authorization.jwt.secret must be at least 32 bytes for HS256");
        }
        if (expirationDays <= 0) {
            throw new IllegalStateException("authorization.jwt.expiration.days must be greater than zero");
        }
    }
}
