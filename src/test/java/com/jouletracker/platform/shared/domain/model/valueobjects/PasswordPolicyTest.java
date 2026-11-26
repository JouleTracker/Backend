package com.jouletracker.platform.shared.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordPolicy Unit Tests")
class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SecurePass123!",
            "SecurePass123#",
            "Secure-Pass123",
            "Secure_Pass123",
            "Secure.Pass123",
            "Secure+Pass123",
            "Joule#Pass-2026_+.",
            "Ab1@5678",
            "Pass/Word=2026",
            "Pass(Word)^2026",
            "Pass~Word[2026]"
    })
    @DisplayName("Should accept strong passwords containing standard printable special characters")
    void shouldAcceptValidPasswordsWithStandardSpecialCharacters(String validPassword) {
        assertThat(PasswordPolicy.isSatisfiedBy(validPassword)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "Ab1#567",
            "lowercase123#",
            "UPPERCASE123#",
            "NoDigitsHere#",
            "NoSpecialChar123",
            "Space Only 1234"
    })
    @DisplayName("Should reject passwords violating length, complexity, or special character rules")
    void shouldRejectInvalidPasswords(String invalidPassword) {
        assertThat(PasswordPolicy.isSatisfiedBy(invalidPassword)).isFalse();
    }

    @Test
    @DisplayName("Should accept passwords up to 128 characters and reject 129 characters or null")
    void shouldEnforceLengthBoundariesAndNullSafety() {
        String maxValid = "Aa1#" + "a".repeat(124);
        String tooLong = "Aa1#" + "a".repeat(125);

        assertThat(maxValid).hasSize(128);
        assertThat(tooLong).hasSize(129);
        assertThat(PasswordPolicy.isSatisfiedBy(maxValid)).isTrue();
        assertThat(PasswordPolicy.isSatisfiedBy(tooLong)).isFalse();
        assertThat(PasswordPolicy.isSatisfiedBy(null)).isFalse();
    }

    @Test
    @DisplayName("Should expose Spanish policy validation message")
    void shouldProvideSpanishPolicyMessage() {
        assertThat(PasswordPolicy.POLICY_MESSAGE)
                .contains("La contraseña debe tener entre 8 y 128 caracteres")
                .contains("mayúscula")
                .contains("minúscula")
                .contains("número")
                .contains("carácter especial");
    }
}
