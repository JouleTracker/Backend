package com.jouletracker.platform.iam.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.iam.application.out.AuthenticatedUserResult;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.application.usecases.SignInUseCase;
import com.jouletracker.platform.iam.application.usecases.SignUpUseCase;
import com.jouletracker.platform.iam.application.usecases.VerifyEmailUseCase;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
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

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        var controller = new AuthenticationController(signUpUseCase, signInUseCase, verifyEmailUseCase);
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
    @DisplayName("POST /api/v1/authentication/sign-up should return 400 Bad Request on invalid email")
    void shouldReturnBadRequestOnMalformedEmail() throws Exception {
        var request = new SignUpRequest("invalid-email", "SecurePass123!", "John", "Doe");

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
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

        when(signInUseCase.handle(any())).thenReturn(Result.failure(ApplicationError.unauthorized("Invalid email or password")));

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
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
}
