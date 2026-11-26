package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ResetPasswordCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordUseCaseImpl Unit Tests (TS-03)")
class ResetPasswordUseCaseImplTest {

    private static final Long TEST_USER_ID = 1L;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    private ResetPasswordUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCaseImpl(tokenRepository, userRepository, passwordHashingService);
    }

    @Test
    @DisplayName("Should reset password with BCrypt hash and consume single-use token")
    void shouldResetPasswordSuccessfully() {
        var userId = UserId.of(TEST_USER_ID);
        var token = PasswordResetToken.create(userId, "valid-token", Duration.ofMinutes(30));
        var user = new User(
                userId,
                EmailAddress.of("john.doe@jouletracker.com"),
                PasswordHash.of("$2a$12$oldHash"),
                "John",
                "Doe",
                UserStatus.ACTIVE,
                true,
                Set.of(Role.ROLE_USER)
        );

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordHashingService.hash("NewSecurePass123!")).thenReturn(PasswordHash.of("$2a$12$newHash"));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = useCase.handle(new ResetPasswordCommand("valid-token", "NewSecurePass123!"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(user.getPasswordHash().value()).isEqualTo("$2a$12$newHash");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("Should reject expired password reset token")
    void shouldRejectExpiredToken() {
        var expiredToken = new PasswordResetToken(
                1L,
                "expired-token",
                UserId.of(TEST_USER_ID),
                Instant.now().minusSeconds(60),
                null
        );
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        var result = useCase.handle(new ResetPasswordCommand("expired-token", "NewSecurePass123!"));

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
        verify(passwordHashingService, never()).hash(any());
    }

    @Test
    @DisplayName("Should reject already-used password reset token (single-use enforcement)")
    void shouldRejectAlreadyUsedToken() {
        var usedToken = new PasswordResetToken(
                1L,
                "used-token",
                UserId.of(TEST_USER_ID),
                Instant.now().plusSeconds(300),
                Instant.now().minusSeconds(10)
        );
        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        var result = useCase.handle(new ResetPasswordCommand("used-token", "NewSecurePass123!"));

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject unknown password reset token")
    void shouldRejectUnknownToken() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        var result = useCase.handle(new ResetPasswordCommand("unknown", "NewSecurePass123!"));

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject weak new password before looking up token")
    void shouldRejectWeakPassword() {
        var result = useCase.handle(new ResetPasswordCommand("valid-token", "weak"));

        assertThat(result.isFailure()).isTrue();
        verify(tokenRepository, never()).findByToken(any());
    }
}
