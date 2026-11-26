package com.jouletracker.platform.iam.infrastructure.email.brevo;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;

/**
 * Configuration properties for the Brevo transactional email integration.
 */
@Validated
@ConfigurationProperties(prefix = "brevo")
public class BrevoProperties {

    private static final List<String> PLACEHOLDER_MARKERS = List.of(
            "change_me",
            "changeme",
            "placeholder",
            "your_brevo",
            "your-brevo",
            "your_api_key",
            "your-api-key",
            "replace_me",
            "<brevo"
    );

    private String apiKey = "";
    private String senderEmail = "jouletacker@gmail.com";
    private String senderName = "JouleTracker";
    private String apiUrl = "https://api.brevo.com/v3/smtp/email";

    @Min(1)
    private int connectTimeoutSeconds = 5;

    @Min(1)
    private int readTimeoutSeconds = 10;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    /**
     * Checks whether the configured Brevo API key is null or blank.
     *
     * @return true if no API key is configured
     */
    public boolean hasMissingApiKey() {
        return apiKey == null || apiKey.isBlank();
    }

    /**
     * Checks whether the configured Brevo API key matches a common placeholder pattern
     * (for example, the default value shipped in {@code .env.example}).
     *
     * @return true if the API key looks like an unreplaced placeholder
     */
    public boolean hasPlaceholderApiKey() {
        if (hasMissingApiKey()) {
            return false;
        }
        String normalized = apiKey.trim().toLowerCase(Locale.ROOT);
        return PLACEHOLDER_MARKERS.stream().anyMatch(normalized::contains);
    }
}
