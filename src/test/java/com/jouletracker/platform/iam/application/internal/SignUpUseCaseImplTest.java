package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.SignUpCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.entities.EmailVerificationToken;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.EmailVerificationTokenRepository;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignUpUseCaseImpl Unit Tests")
class SignUpUseCaseImplTest {

    private static final UUID TEST_USER_ID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private EmailService emailService;

    private SignUpUseCaseImpl signUpUseCase;

    @BeforeEach
    void setUp() {
        signUpUseCase = new SignUpUseCaseImpl(
                userRepository,
                passwordHashingService,
                emailVerificationTokenRepository,
                emailService
        );
    }

    @Test
    @DisplayName("Should successfully register a new user and dispatch verification email")
    void shouldRegisterNewUserSuccessfully() {
        var command = new SignUpCommand(
                "john.doe@jouletracker.com",
                "SecurePass123!",
                "John",
                "Doe"
        );

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordHashingService.hash("SecurePass123!")).thenReturn(PasswordHash.of("$2a$12$hashed"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.assignId(UserId.of(TEST_USER_ID));
            return userToSave;
        });

        var result = signUpUseCase.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var userResult = result.toOptional().orElseThrow();
        assertThat(userResult.email()).isEqualTo("john.doe@jouletracker.com");
        assertThat(userResult.emailVerified()).isFalse();

        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(
                eq(EmailAddress.of("john.doe@jouletracker.com")),
                eq("John Doe"),
                anyString()
        );
    }

    @Test
    @DisplayName("Should reject registration when email is already in use")
    void shouldRejectDuplicateEmail() {
        var command = new SignUpCommand(
                "existing@jouletracker.com",
                "SecurePass123!",
                "John",
                "Doe"
        );

        when(userRepository.existsByEmail(any())).thenReturn(true);

        var result = signUpUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should reject registration when email format is invalid")
    void shouldRejectInvalidEmailFormat() {
        var command = new SignUpCommand(
                "not-an-email",
                "SecurePass123!",
                "John",
                "Doe"
        );

        var result = signUpUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject a weak password before persistence")
    void shouldRejectWeakPassword() {
        var command = new SignUpCommand("john@jouletracker.com", "weak", "John", "Doe");

        when(userRepository.existsByEmail(any())).thenReturn(false);

        var result = signUpUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(passwordHashingService, never()).hash(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should propagate email delivery failure rather than report registration success")
    void shouldPropagateEmailDeliveryFailure() {
        var command = new SignUpCommand("john@jouletracker.com", "SecurePass123!", "John", "Doe");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordHashingService.hash(any())).thenReturn(PasswordHash.of("$2a$12$hashed"));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.assignId(UserId.of(TEST_USER_ID));
            return user;
        });
        doThrow(new IllegalStateException("Brevo unavailable"))
                .when(emailService).sendVerificationEmail(any(), any(), any());

        assertThatThrownBy(() -> signUpUseCase.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Brevo unavailable");
    }

    @Test
    @DisplayName("Should propagate persistence failure")
    void shouldPropagatePersistenceFailure() {
        var command = new SignUpCommand("john@jouletracker.com", "SecurePass123!", "John", "Doe");
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordHashingService.hash(any())).thenReturn(PasswordHash.of("$2a$12$hashed"));
        when(userRepository.save(any(User.class))).thenThrow(new IllegalStateException("Database unavailable"));

        assertThatThrownBy(() -> signUpUseCase.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database unavailable");
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }
}
