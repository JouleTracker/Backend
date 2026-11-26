package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ForgotPasswordCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.PasswordResetToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.PasswordResetTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ForgotPasswordUseCaseImpl Unit Tests (TS-03)")
class ForgotPasswordUseCaseImplTest {

    private static final UUID TEST_USER_ID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private ForgotPasswordUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ForgotPasswordUseCaseImpl(userRepository, tokenRepository, emailService);
    }

    @Test
    @DisplayName("Should generate a recovery token, invalidate old tokens, and dispatch reset email")
    void shouldGenerateTokenAndSendResetEmail() {
        var email = EmailAddress.of("john.doe@jouletracker.com");
        var user = new User(
                UserId.of(TEST_USER_ID),
                email,
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                UserStatus.ACTIVE,
                true,
                Set.of(Role.ROLE_USER)
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var result = useCase.handle(new ForgotPasswordCommand("john.doe@jouletracker.com"));

        assertThat(result.isSuccess()).isTrue();
        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(email), eq("John Doe"), anyString());
    }

    @Test
    @DisplayName("Should return generic success without sending email when email is not registered")
    void shouldReturnGenericSuccessWhenEmailNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        var result = useCase.handle(new ForgotPasswordCommand("missing@jouletracker.com"));

        assertThat(result.isSuccess()).isTrue();
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should return validation error when email format is invalid")
    void shouldFailOnMalformedEmail() {
        var result = useCase.handle(new ForgotPasswordCommand("invalid-email"));

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).findByEmail(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should skip sending reset email when account is deactivated or banned")
    void shouldSkipSendingEmailForInactiveOrBannedUser() {
        var email = EmailAddress.of("deactivated@jouletracker.com");
        var user = new User(
                UserId.of(TEST_USER_ID),
                email,
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                UserStatus.INACTIVE,
                true,
                Instant.now(),
                Set.of(Role.ROLE_USER)
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var result = useCase.handle(new ForgotPasswordCommand("deactivated@jouletracker.com"));

        assertThat(result.isSuccess()).isTrue();
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any());
    }
}
