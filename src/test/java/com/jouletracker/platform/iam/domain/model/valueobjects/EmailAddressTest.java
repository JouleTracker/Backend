package com.jouletracker.platform.iam.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailAddress Value Object Unit Tests")
class EmailAddressTest {

    @Test
    @DisplayName("Should create valid email and normalize to lower-case")
    void shouldCreateValidEmailAndNormalize() {
        var email = EmailAddress.of("John.Doe@JouleTracker.COM");
        assertThat(email.value()).isEqualTo("john.doe@jouletracker.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "plainaddress", "missing@domain", "@nodomain.com", "user@.com"})
    @DisplayName("Should throw IllegalArgumentException for malformed emails")
    void shouldRejectInvalidEmailFormats(String invalidEmail) {
        assertThatThrownBy(() -> EmailAddress.of(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw NullPointerException when email is null")
    void shouldThrowWhenNull() {
        assertThatThrownBy(() -> EmailAddress.of(null))
                .isInstanceOf(NullPointerException.class);
    }
}
