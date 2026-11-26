package com.jouletracker.platform.shared.infrastructure.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ApplicationProperties Configuration Validation Tests (US-04 & TS-03)")
class ApplicationPropertiesTest {

    @Test
    @DisplayName("Should build normalized verification and password reset URLs from FRONTEND_BASE_URL")
    void shouldBuildUrlsFromFrontendBaseUrl() {
        var props = new ApplicationProperties();
        props.setFrontendUrl("https://app.jouletracker.com/");

        props.validateConfiguration();

        assertThat(props.getNormalizedFrontendBaseUrl()).isEqualTo("https://app.jouletracker.com");
        assertThat(props.buildEmailVerificationUrl("abc123"))
                .isEqualTo("https://app.jouletracker.com/verify-email?token=abc123");
        assertThat(props.buildPasswordResetUrl("xyz789"))
                .isEqualTo("https://app.jouletracker.com/reset-password?token=xyz789");
    }

    @Test
    @DisplayName("Should fail fast when FRONTEND_BASE_URL is blank")
    void shouldFailFastWhenFrontendUrlIsBlank() {
        var props = new ApplicationProperties();
        props.setFrontendUrl("   ");

        assertThatThrownBy(props::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FRONTEND_BASE_URL");
    }

    @Test
    @DisplayName("Should fail fast when FRONTEND_BASE_URL is not a valid HTTP/HTTPS URL")
    void shouldFailFastWhenFrontendUrlIsMalformed() {
        var props = new ApplicationProperties();
        props.setFrontendUrl("ftp://invalid-scheme");

        assertThatThrownBy(props::validateConfiguration)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTP or HTTPS");
    }
}
