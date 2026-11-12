package com.jouletracker.platform.iam.infrastructure.tokens.jwt;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenService Unit Tests")
class JwtTokenServiceTest {

    private static final UUID TEST_USER_ID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");
    private JwtTokenService tokenService;

    @BeforeEach
    void setUp() {
        var properties = new JwtProperties();
        properties.setSecret("VerySecretKeyForTestingJwtTokenServiceAtLeast32Bytes!");
        properties.setExpirationDays(7);
        tokenService = new JwtTokenService(properties);
    }

    @Test
    @DisplayName("Should generate valid token and extract claims")
    void shouldGenerateAndExtractClaims() {
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("user@jouletracker.com"),
                PasswordHash.of("$2a$12$..."),
                "Alice",
                "Smith",
                Set.of(Role.ROLE_USER, Role.ROLE_ADMIN),
                true
        );

        String token = tokenService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(tokenService.validateToken(token)).isTrue();
        assertThat(tokenService.extractUsername(token)).isEqualTo("user@jouletracker.com");
        assertThat(tokenService.extractUserId(token)).isEqualTo(TEST_USER_ID);
        assertThat(tokenService.extractRoles(token)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should return false for malformed or tampered token")
    void shouldRejectInvalidToken() {
        assertThat(tokenService.validateToken("invalid.jwt.token")).isFalse();
        assertThat(tokenService.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("Should reject an expired token")
    void shouldRejectExpiredToken() {
        var properties = new JwtProperties();
        properties.setSecret("VerySecretKeyForTestingJwtTokenServiceAtLeast32Bytes!");
        properties.setExpirationDays(-1);
        var expiredTokenService = new JwtTokenService(properties);
        var user = new User(UserId.of(TEST_USER_ID), EmailAddress.of("user@jouletracker.com"),
                PasswordHash.of("$2a$12$..."), "Alice", "Smith", Set.of(Role.ROLE_USER), true);

        assertThat(expiredTokenService.validateToken(expiredTokenService.generateToken(user))).isFalse();
    }
}
