package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.ResendVerificationCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResendVerificationUseCaseImpl Unit Tests (US-04)")
class ResendVerificationUseCaseImplTest {

    private static final Long TEST_USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    private ResendVerificationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResendVerificationUseCaseImpl(userRepository, tokenRepository, emailService);
    }

    @Test
    @DisplayName("Should invalidate old tokens and resend verification email for unverified user")
    void shouldResendVerificationEmailSuccessfully() {
        var email = EmailAddress.of("pending@jouletracker.com");
        var user = new User(
                UserId.of(TEST_USER_ID),
                email,
                PasswordHash.of("$2a$12$hashed"),
                "Jane",
                "Doe",
                UserStatus.PENDING_VERIFICATION,
                false,
                Set.of(Role.ROLE_USER)
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var result = useCase.handle(new ResendVerificationCommand("pending@jouletracker.com"));

        assertThat(result.isSuccess()).isTrue();
        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq(email), eq("Jane Doe"), anyString());
    }

    @Test
    @DisplayName("Should return conflict when user account is already verified")
    void shouldReturnConflictWhenAlreadyVerified() {
        var email = EmailAddress.of("verified@jouletracker.com");
        var user = new User(
                UserId.of(TEST_USER_ID),
                email,
                PasswordHash.of("$2a$12$hashed"),
                "Jane",
                "Doe",
                UserStatus.ACTIVE,
                true,
                Set.of(Role.ROLE_USER)
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        var result = useCase.handle(new ResendVerificationCommand("verified@jouletracker.com"));

        assertThat(result.isFailure()).isTrue();
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should return not found when account does not exist")
    void shouldReturnNotFoundForUnknownEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        var result = useCase.handle(new ResendVerificationCommand("missing@jouletracker.com"));

        assertThat(result.isFailure()).isTrue();
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should reject malformed email address")
    void shouldRejectMalformedEmail() {
        var result = useCase.handle(new ResendVerificationCommand("bad-email"));

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).findByEmail(any());
    }
}
