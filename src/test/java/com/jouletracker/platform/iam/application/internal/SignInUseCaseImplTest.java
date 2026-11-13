package com.jouletracker.platform.iam.application.internal;

import com.jouletracker.platform.iam.application.commands.SignInCommand;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.iam.domain.services.TokenService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignInUseCaseImpl Unit Tests")
class SignInUseCaseImplTest {

    private static final Long TEST_USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private TokenService tokenService;

    private SignInUseCaseImpl signInUseCase;

    @BeforeEach
    void setUp() {
        signInUseCase = new SignInUseCaseImpl(
                userRepository,
                passwordHashingService,
                tokenService
        );
    }

    @Test
    @DisplayName("Should authenticate verified user and return JWT")
    void shouldAuthenticateVerifiedUser() {
        var command = new SignInCommand("john.doe@jouletracker.com", "SecurePass123!");
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("john.doe@jouletracker.com"),
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                Set.of(Role.ROLE_USER),
                true
        );

        when(userRepository.findByEmail(EmailAddress.of("john.doe@jouletracker.com")))
                .thenReturn(Optional.of(user));
        when(passwordHashingService.matches(eq("SecurePass123!"), eq(user.getPasswordHash())))
                .thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("mocked.jwt.token");

        var result = signInUseCase.handle(command);

        assertThat(result.isSuccess()).isTrue();
        var authResult = result.toOptional().orElseThrow();
        assertThat(authResult.token()).isEqualTo("mocked.jwt.token");
        assertThat(authResult.tokenType()).isEqualTo("Bearer");
        assertThat(authResult.user().email()).isEqualTo("john.doe@jouletracker.com");
    }

    @Test
    @DisplayName("Should reject authentication when user does not exist")
    void shouldRejectWhenUserNotFound() {
        var command = new SignInCommand("unknown@jouletracker.com", "SecurePass123!");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        var result = signInUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should reject authentication when password does not match")
    void shouldRejectWhenPasswordMismatch() {
        var command = new SignInCommand("john.doe@jouletracker.com", "WrongPassword");
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("john.doe@jouletracker.com"),
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                Set.of(Role.ROLE_USER),
                true
        );

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordHashingService.matches("WrongPassword", user.getPasswordHash())).thenReturn(false);

        var result = signInUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should block authentication when email is not confirmed")
    void shouldBlockWhenEmailNotConfirmed() {
        var command = new SignInCommand("john.doe@jouletracker.com", "SecurePass123!");
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("john.doe@jouletracker.com"),
                PasswordHash.of("$2a$12$hashed"),
                "John",
                "Doe",
                Set.of(Role.ROLE_USER),
                false
        );

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordHashingService.matches("SecurePass123!", user.getPasswordHash())).thenReturn(true);

        var result = signInUseCase.handle(command);

        assertThat(result.isFailure()).isTrue();
        verify(tokenService, never()).generateToken(any());
    }
}
