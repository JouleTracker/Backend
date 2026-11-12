package com.jouletracker.platform.iam.infrastructure.hashing.bcrypt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BCryptPasswordHashingService Unit Tests")
class BCryptPasswordHashingServiceTest {

    private BCryptPasswordHashingService hashingService;

    @BeforeEach
    void setUp() {
        hashingService = new BCryptPasswordHashingService(new BCryptPasswordEncoder(10));
    }

    @Test
    @DisplayName("Should hash raw password and verify match")
    void shouldHashAndMatchPassword() {
        var rawPassword = "SecretPassword123!";
        var hash = hashingService.hash(rawPassword);

        assertThat(hash.value()).isNotBlank();
        assertThat(hash.value()).startsWith("$2a$");
        assertThat(hashingService.matches(rawPassword, hash)).isTrue();
        assertThat(hashingService.matches("WrongPassword", hash)).isFalse();
    }
}
