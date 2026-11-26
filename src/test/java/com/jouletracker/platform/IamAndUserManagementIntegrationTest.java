package com.jouletracker.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.EmailService;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.iam.domain.services.TokenService;
import com.jouletracker.platform.iam.interfaces.rest.resources.ForgotPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResendVerificationRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.ResetPasswordRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import com.jouletracker.platform.users.interfaces.rest.resources.ChangePasswordRequest;
import com.jouletracker.platform.users.interfaces.rest.resources.UpdateUserStatusRequest;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests verifying IAM (TS-03, US-04) and User Management (US-25, US-40, US-49)
 * flows through the real Spring Security JWT filter chain, BCrypt hashing, and JPA persistence layer.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("IAM & User Management End-to-End Integration Tests")
class IamAndUserManagementIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHashingService passwordHashingService;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private EmailService emailService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
        reset(emailService);
    }

    private User createPersistedUser(String email, String rawPassword, boolean verified, Set<Role> roles) {
        User user = User.create(
                EmailAddress.of(email),
                passwordHashingService.hash(rawPassword),
                "Test",
                "User",
                roles
        );
        if (verified) {
            user.confirmEmail();
        }
        return userRepository.save(user);
    }

    @Test
    @DisplayName("[TS-03] Full forgot-password -> reset-password -> sign-in flow and single-use token enforcement")
    void shouldCompletePasswordRecoveryAndEnforceSingleUseToken() throws Exception {
        String email = "recover-" + UUID.randomUUID() + "@jouletracker.com";
        createPersistedUser(email, "OldPass123!", true, Set.of(Role.ROLE_USER));

        mockMvc.perform(post("/api/v1/authentication/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(eq(EmailAddress.of(email)), any(), tokenCaptor.capture());
        String rawResetToken = tokenCaptor.getValue();
        assertThat(rawResetToken).isNotBlank();

        mockMvc.perform(post("/api/v1/authentication/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(rawResetToken, "NewPass#456-_.+"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La contraseña ha sido restablecida correctamente."));

        // Reusing the same token must fail with 422 Unprocessable Entity
        mockMvc.perform(post("/api/v1/authentication/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(rawResetToken, "AnotherPass789#"))))
                .andExpect(status().isUnprocessableEntity());

        // Sign-in with new password succeeds
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignInRequest(email, "NewPass#456-_.+"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("[US-04] Should resend verification email for unverified account and return 409 when already verified")
    void shouldResendVerificationAndRejectWhenVerified() throws Exception {
        String unverifiedEmail = "unverified-" + UUID.randomUUID() + "@jouletracker.com";
        createPersistedUser(unverifiedEmail, "SecurePass123!", false, Set.of(Role.ROLE_USER));

        mockMvc.perform(post("/api/v1/authentication/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResendVerificationRequest(unverifiedEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("El correo de verificación ha sido reenviado correctamente."));

        verify(emailService).sendVerificationEmail(eq(EmailAddress.of(unverifiedEmail)), any(), any());

        String verifiedEmail = "verified-" + UUID.randomUUID() + "@jouletracker.com";
        createPersistedUser(verifiedEmail, "SecurePass123!", true, Set.of(Role.ROLE_USER));

        mockMvc.perform(post("/api/v1/authentication/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResendVerificationRequest(verifiedEmail))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("[US-25] Should allow user to change own password and forbid modifying another user's password")
    void shouldEnforceOwnershipAndChangePassword() throws Exception {
        User owner = createPersistedUser("owner-" + UUID.randomUUID() + "@jouletracker.com", "InitPass123!", true, Set.of(Role.ROLE_USER));
        User other = createPersistedUser("other-" + UUID.randomUUID() + "@jouletracker.com", "InitPass123!", true, Set.of(Role.ROLE_USER));
        String ownerJwt = tokenService.generateToken(owner);

        // Unauthenticated -> 401
        mockMvc.perform(put("/api/v1/users/{id}/password", owner.getId().value())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("InitPass123!", "UpdatedPass456#"))))
                .andExpect(status().isUnauthorized());

        // Attempting to change another user's password -> 403
        mockMvc.perform(put("/api/v1/users/{id}/password", other.getId().value())
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("InitPass123!", "UpdatedPass456#"))))
                .andExpect(status().isForbidden());

        // Weak new password -> 400 with Spanish validation message
        mockMvc.perform(put("/api/v1/users/{id}/password", owner.getId().value())
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("InitPass123!", "WeakPass123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.invalidParams[0].message").value(PasswordPolicy.POLICY_MESSAGE));

        // Wrong current password -> 400
        mockMvc.perform(put("/api/v1/users/{id}/password", owner.getId().value())
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("WrongPass123!", "UpdatedPass456#"))))
                .andExpect(status().isBadRequest());

        // Valid change with common special chars (#, -, _, ., +) -> 200
        mockMvc.perform(put("/api/v1/users/{id}/password", owner.getId().value())
                        .header("Authorization", "Bearer " + ownerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("InitPass123!", "Updated#Pass-456_.+"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("La contraseña se actualizó correctamente."));
    }

    @Test
    @DisplayName("[US-40] Deactivating account immediately blocks existing JWT and prevents future sign-in")
    void shouldSoftDeactivateAccountAndImmediatelyBlockAccess() throws Exception {
        String email = "deactivate-" + UUID.randomUUID() + "@jouletracker.com";
        User user = createPersistedUser(email, "SecurePass123!", true, Set.of(Role.ROLE_USER));
        String jwt = tokenService.generateToken(user);

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", user.getId().value())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(UserStatus.INACTIVE.name()))
                .andExpect(jsonPath("$.deactivatedAt").isNotEmpty());

        // Existing JWT must immediately fail on protected endpoints
        mockMvc.perform(put("/api/v1/users/{id}/password", user.getId().value())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("SecurePass123!", "NewPass456!"))))
                .andExpect(status().isUnauthorized());

        // Sign-in must be rejected with 403 Forbidden
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignInRequest(email, "SecurePass123!"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[US-49] Admin moderation restricts endpoint to ROLE_ADMIN, blocks self-ban, and immediately blocks banned users")
    void shouldEnforceAdminModerationRulesAndBlockBannedUser() throws Exception {
        User admin = createPersistedUser("admin-" + UUID.randomUUID() + "@jouletracker.com", "AdminPass123!", true, Set.of(Role.ROLE_ADMIN));
        String targetEmail = "target-" + UUID.randomUUID() + "@jouletracker.com";
        User target = createPersistedUser(targetEmail, "UserPass123!", true, Set.of(Role.ROLE_USER));

        String adminJwt = tokenService.generateToken(admin);
        String targetJwt = tokenService.generateToken(target);

        // Regular ROLE_USER attempting admin moderation -> 403 Forbidden
        mockMvc.perform(patch("/api/v1/users/{id}/status", admin.getId().value())
                        .header("Authorization", "Bearer " + targetJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest("BANNED", "Attempt"))))
                .andExpect(status().isForbidden());

        // Admin attempting to ban themselves -> 403 Forbidden
        mockMvc.perform(patch("/api/v1/users/{id}/status", admin.getId().value())
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest("BANNED", "Self ban"))))
                .andExpect(status().isForbidden());

        // Admin banning target user -> 200 OK
        mockMvc.perform(patch("/api/v1/users/{id}/status", target.getId().value())
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserStatusRequest("BANNED", "Policy violation"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BANNED"));

        // Banned user's existing JWT is immediately rejected on authenticated endpoints
        mockMvc.perform(put("/api/v1/users/{id}/password", target.getId().value())
                        .header("Authorization", "Bearer " + targetJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest("UserPass123!", "NewPass456!"))))
                .andExpect(status().isUnauthorized());

        // Banned user cannot sign in
        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignInRequest(targetEmail, "UserPass123!"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[TS-01] Should register user with single-word name (blank lastName) and dispatch verification email with single name")
    void shouldRegisterUserWithBlankLastName() throws Exception {
        String email = "rodrigo-" + UUID.randomUUID() + "@jouletracker.com";
        var signUpRequest = new SignUpRequest(email, "Joule#Pass-2026", "rodrigo", "");

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("rodrigo"))
                .andExpect(jsonPath("$.lastName").value(""));

        verify(emailService).sendVerificationEmail(eq(EmailAddress.of(email)), eq("rodrigo"), any());
    }
}
