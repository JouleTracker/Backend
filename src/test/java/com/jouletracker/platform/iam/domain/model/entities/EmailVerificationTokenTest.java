package com.jouletracker.platform.iam.domain.model.entities;

import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.VerificationTokenStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EmailVerificationToken Entity Unit Tests")
class EmailVerificationTokenTest {

    private static final UUID USER_UUID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");

    @Test
    @DisplayName("Should create pending valid token")
    void shouldCreatePendingValidToken() {
        var token = EmailVerificationToken.create(UserId.of(USER_UUID), "token-123", Duration.ofHours(24));

        assertThat(token.isValid()).isTrue();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isUsed()).isFalse();
        assertThat(token.getStatus()).isEqualTo(VerificationTokenStatus.PENDING);
    }

    @Test
    @DisplayName("Should mark token as used successfully")
    void shouldMarkTokenAsUsed() {
        var token = EmailVerificationToken.create(UserId.of(USER_UUID), "token-123", Duration.ofHours(24));
        token.markAsUsed();

        assertThat(token.isUsed()).isTrue();
        assertThat(token.isValid()).isFalse();
        assertThat(token.getStatus()).isEqualTo(VerificationTokenStatus.USED);
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when attempting to use already consumed token")
    void shouldThrowWhenMarkingUsedTwice() {
        var token = EmailVerificationToken.create(UserId.of(USER_UUID), "token-123", Duration.ofHours(24));
        token.markAsUsed();

        assertThatThrownBy(token::markAsUsed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    @DisplayName("Should recognize expired token and prevent usage")
    void shouldDetectExpiredToken() {
        var expiredToken = new EmailVerificationToken(
                1L,
                "token-expired",
                UserId.of(USER_UUID),
                Instant.now().minus(Duration.ofHours(1)),
                null
        );

        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(expiredToken.isValid()).isFalse();
        assertThat(expiredToken.getStatus()).isEqualTo(VerificationTokenStatus.EXPIRED);

        assertThatThrownBy(expiredToken::markAsUsed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }
}
