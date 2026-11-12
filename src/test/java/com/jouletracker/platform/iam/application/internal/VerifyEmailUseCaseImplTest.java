package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.VerifyEmailCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyEmailUseCaseImpl Unit Tests")
class VerifyEmailUseCaseImplTest {

    private static final UUID TEST_USER_ID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    private VerifyEmailUseCaseImpl verifyEmailUseCase;

    @BeforeEach
    void setUp() {
        verifyEmailUseCase = new VerifyEmailUseCaseImpl(tokenRepository, userRepository);
    }

    @Test
    @DisplayName("Should successfully confirm user email with valid token")
    void shouldVerifyEmailSuccessfully() {
        var command = new VerifyEmailCommand("valid-token-123");
        var token = EmailVerificationToken.create(UserId.of(TEST_USER_ID), "valid-token-123", Duration.ofHours(24));

        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("john.doe@jouletracker.com"),
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                Set.of(Role.ROLE_USER),
                false
        );

        when(tokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(token));
        when(userRepository.findById(UserId.of(TEST_USER_ID))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        var result = verifyEmailUseCase.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().emailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();

        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should reject verification when token does not exist")
    void shouldRejectWhenTokenNotFound() {
        var command = new VerifyEmailCommand("non-existent-token");
        when(tokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        var result = verifyEmailUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject verification when token has expired")
    void shouldRejectWhenTokenExpired() {
        var command = new VerifyEmailCommand("expired-token");
        var expiredToken = new EmailVerificationToken(
                1L,
                "expired-token",
                UserId.of(TEST_USER_ID),
                Instant.now().minus(Duration.ofHours(2)),
                null
        );

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        var result = verifyEmailUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject verification when token has already been used")
    void shouldRejectWhenTokenAlreadyUsed() {
        var command = new VerifyEmailCommand("used-token");
        var usedToken = new EmailVerificationToken(
                1L,
                "used-token",
                UserId.of(TEST_USER_ID),
                Instant.now().plus(Duration.ofHours(2)),
                Instant.now().minus(Duration.ofMinutes(10))
        );

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        var result = verifyEmailUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
    }
}
