package com.jouletracker.platform.iam.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.iam.application.out.AuthenticatedUserResult;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.application.usecases.ForgotPasswordUseCase;
import com.jouletracker.platform.iam.application.usecases.ResendVerificationUseCase;
import com.jouletracker.platform.iam.application.usecases.ResetPasswordUseCase;
import com.jouletracker.platform.iam.application.usecases.SignInUseCase;
import com.jouletracker.platform.iam.application.usecases.SignUpUseCase;
import com.jouletracker.platform.iam.application.usecases.VerifyEmailUseCase;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.interfaces.rest.resources.ForgotPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResendVerificationRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResetPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import com.jouletracker.platform.shared.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Unit/Web Tests")
class AuthenticationControllerTest {

    private static final Long TEST_USER_ID = 1L;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SignUpUseCase signUpUseCase;

    @Mock
    private SignInUseCase signInUseCase;

    @Mock
    private VerifyEmailUseCase verifyEmailUseCase;

    @Mock
    private ResendVerificationUseCase resendVerificationUseCase;

    @Mock
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        var controller = new AuthenticationController(
                signUpUseCase,
                signInUseCase,
                verifyEmailUseCase,
                resendVerificationUseCase,
                forgotPasswordUseCase,
                resetPasswordUseCase
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-up should return 201 Created on valid input")
    void shouldReturnCreatedOnSignUp() throws Exception {
        var request = new SignUpRequest("john.doe@jouletracker.com", "SecurePass123!", "John", "Doe");
        var userResult = new UserResult(TEST_USER_ID, "john.doe@jouletracker.com", "John", "Doe", Set.of(Role.ROLE_USER), false);

        when(signUpUseCase.handle(any())).thenReturn(Result.success(userResult));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@jouletracker.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-up should return 201 Created on password with common special characters (#, -, _, ., +)")
    void shouldAcceptCommonSpecialCharactersOnSignUp() throws Exception {
        var request = new SignUpRequest("john.doe@jouletracker.com", "Joule#Pass-2026_+.", "John", "Doe");
        var userResult = new UserResult(TEST_USER_ID, "john.doe@jouletracker.com", "John", "Doe", Set.of(Role.ROLE_USER), false);

        when(signUpUseCase.handle(any())).thenReturn(Result.success(userResult));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@jouletracker.com"));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-up should accept blank or null lastName for single-word names")
    void shouldAcceptOptionalLastNameOnSignUp() throws Exception {
        var blankLastNameRequest = new SignUpRequest("rodrigo@jouletracker.com", "Joule#Pass-2026", "rodrigo", "");
        var nullLastNameRequest = new SignUpRequest("rodrigo@jouletracker.com", "Joule#Pass-2026", "rodrigo", null);
        var userResult = new UserResult(TEST_USER_ID, "rodrigo@jouletracker.com", "rodrigo", "", Set.of(Role.ROLE_USER), false);

        when(signUpUseCase.handle(any())).thenReturn(Result.success(userResult));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankLastNameRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("rodrigo"))
                .andExpect(jsonPath("$.lastName").value(""));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullLastNameRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("rodrigo"))
                .andExpect(jsonPath("$.lastName").value(""));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-up should return 400 Bad Request with Spanish message on invalid password")
    void shouldReturnSpanishValidationMessageOnWeakPasswordSignUp() throws Exception {
        var request = new SignUpRequest("john.doe@jouletracker.com", "SecurePass123", "John", "Doe");

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.invalidParams[0].field").value("password"))
                .andExpect(jsonPath("$.invalidParams[0].message").value(PasswordPolicy.POLICY_MESSAGE));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-up should return 400 Bad Request on invalid email")
    void shouldReturnBadRequestOnMalformedEmail() throws Exception {
        var request = new SignUpRequest("invalid-email", "SecurePass123!", "John", "Doe");

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.invalidParams[0].message").value("El correo electrónico debe tener un formato válido"));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-in should return 200 OK with Bearer token")
    void shouldReturnOkOnSignIn() throws Exception {
        var request = new SignInRequest("john.doe@jouletracker.com", "SecurePass123!");
        var userResult = new UserResult(TEST_USER_ID, "john.doe@jouletracker.com", "John", "Doe", Set.of(Role.ROLE_USER), true);
        var authResult = new AuthenticatedUserResult("mocked.jwt.token", "Bearer", userResult);

        when(signInUseCase.handle(any())).thenReturn(Result.success(authResult));

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john.doe@jouletracker.com"));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/sign-in should return 401 Unauthorized on invalid credentials")
    void shouldReturnUnauthorizedOnBadCredentials() throws Exception {
        var request = new SignInRequest("john.doe@jouletracker.com", "WrongPassword");

        when(signInUseCase.handle(any())).thenReturn(Result.failure(ApplicationError.unauthorized("Correo electrónico o contraseña inválidos")));

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autorizado"));
    }

    @Test
    @DisplayName("GET /api/v1/authentication/verify-email should return 200 OK on valid token")
    void shouldReturnOkOnVerifyEmail() throws Exception {
        var userResult = new UserResult(TEST_USER_ID, "john.doe@jouletracker.com", "John", "Doe", Set.of(Role.ROLE_USER), true);

        when(verifyEmailUseCase.handle(any())).thenReturn(Result.success(userResult));

        mockMvc.perform(get("/api/v1/authentication/verify-email")
                        .param("token", "valid-verification-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/resend-verification should return 200 OK on valid unverified account")
    void shouldReturnOkOnResendVerification() throws Exception {
        var request = new ResendVerificationRequest("john.doe@jouletracker.com");
        when(resendVerificationUseCase.handle(any())).thenReturn(Result.success("El correo de verificación ha sido reenviado correctamente."));

        mockMvc.perform(post("/api/v1/authentication/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("El correo de verificación ha sido reenviado correctamente."));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/resend-verification should return 409 Conflict when already verified")
    void shouldReturnConflictWhenAlreadyVerified() throws Exception {
        var request = new ResendVerificationRequest("john.doe@jouletracker.com");
        when(resendVerificationUseCase.handle(any())).thenReturn(
                Result.failure(ApplicationError.conflict("EmailVerification", "El correo electrónico de la cuenta ya está verificado"))
        );

        mockMvc.perform(post("/api/v1/authentication/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflicto de recurso"));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/forgot-password should return 200 OK on valid email")
    void shouldReturnOkOnForgotPassword() throws Exception {
        var request = new ForgotPasswordRequest("john.doe@jouletracker.com");
        when(forgotPasswordUseCase.handle(any())).thenReturn(
                Result.success("Si existe una cuenta asociada al correo electrónico proporcionado, se ha enviado un enlace de recuperación de contraseña.")
        );

        mockMvc.perform(post("/api/v1/authentication/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/authentication/forgot-password should return 400 Bad Request on malformed email")
    void shouldReturnBadRequestOnForgotPasswordInvalidEmail() throws Exception {
        var request = new ForgotPasswordRequest("not-an-email");

        mockMvc.perform(post("/api/v1/authentication/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/reset-password should return 200 OK on valid token and password with #-_+")
    void shouldReturnOkOnResetPassword() throws Exception {
        var request = new ResetPasswordRequest("valid-reset-token", "NewSecure#Pass-123_+");
        when(resetPasswordUseCase.handle(any())).thenReturn(Result.success("La contraseña ha sido restablecida correctamente."));

        mockMvc.perform(post("/api/v1/authentication/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La contraseña ha sido restablecida correctamente."));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/reset-password should return 400 Bad Request with Spanish message on weak password")
    void shouldReturnBadRequestOnResetPasswordWeakPassword() throws Exception {
        var request = new ResetPasswordRequest("valid-reset-token", "NoSpecialChar123");

        mockMvc.perform(post("/api/v1/authentication/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.invalidParams[0].field").value("newPassword"))
                .andExpect(jsonPath("$.invalidParams[0].message").value(PasswordPolicy.POLICY_MESSAGE));
    }

    @Test
    @DisplayName("POST /api/v1/authentication/reset-password should return 422 on expired token")
    void shouldReturnUnprocessableEntityOnExpiredResetToken() throws Exception {
        var request = new ResetPasswordRequest("expired-reset-token", "NewSecurePass123!");
        when(resetPasswordUseCase.handle(any())).thenReturn(
                Result.failure(ApplicationError.businessRuleViolation("PASSWORD_RESET_TOKEN_EXPIRED", "El token ha expirado"))
        );

        mockMvc.perform(post("/api/v1/authentication/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Violación de regla de negocio"));
    }
}
