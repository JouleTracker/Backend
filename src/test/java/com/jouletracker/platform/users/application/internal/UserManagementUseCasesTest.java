package com.jouletracker.platform.users.application.internal;

import com.jouletracker.platform.users.application.commands.ChangePasswordCommand;
import com.jouletracker.platform.users.application.commands.DeactivateUserCommand;
import com.jouletracker.platform.users.application.commands.UpdateUserStatusCommand;
import com.jouletracker.platform.users.domain.model.valueobjects.AccountModerationStatus;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import com.jouletracker.platform.users.domain.services.ManagedUserAccountPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Management Context Use Cases Unit Tests (US-25, US-40, US-49)")
class UserManagementUseCasesTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ADMIN_ID = 99L;

    @Mock
    private ManagedUserAccountPort userAccountPort;

    private ChangePasswordUseCaseImpl changePasswordUseCase;
    private DeactivateUserUseCaseImpl deactivateUserUseCase;
    private UpdateUserStatusUseCaseImpl updateUserStatusUseCase;

    @BeforeEach
    void setUp() {
        changePasswordUseCase = new ChangePasswordUseCaseImpl(userAccountPort);
        deactivateUserUseCase = new DeactivateUserUseCaseImpl(userAccountPort);
        updateUserStatusUseCase = new UpdateUserStatusUseCaseImpl(userAccountPort);
    }

    private ManagedUserSnapshot activeUser(Long id) {
        return new ManagedUserSnapshot(
                id,
                "user@jouletracker.com",
                "John",
                "Doe",
                Set.of("ROLE_USER"),
                "ACTIVE",
                true,
                null
        );
    }

    @Test
    @DisplayName("[US-25] Should allow user to change their own password when current password matches")
    void shouldChangeOwnPasswordSuccessfully() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser(USER_ID)));
        when(userAccountPort.verifyPassword(USER_ID, "CurrentPass123!")).thenReturn(true);

        var result = changePasswordUseCase.handle(
                new ChangePasswordCommand(USER_ID, USER_ID, "CurrentPass123!", "NewStrongPass456!")
        );

        assertThat(result.isSuccess()).isTrue();
        verify(userAccountPort).updatePassword(USER_ID, "NewStrongPass456!");
    }

    @Test
    @DisplayName("[US-25] Should forbid changing another user's password")
    void shouldForbidChangingAnotherUsersPassword() {
        var result = changePasswordUseCase.handle(
                new ChangePasswordCommand(USER_ID, OTHER_USER_ID, "CurrentPass123!", "NewStrongPass456!")
        );

        assertThat(result.isFailure()).isTrue();
        verify(userAccountPort, never()).updatePassword(any(), any());
    }

    @Test
    @DisplayName("[US-25] Should reject password change when current password is incorrect")
    void shouldRejectWrongCurrentPassword() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser(USER_ID)));
        when(userAccountPort.verifyPassword(USER_ID, "WrongPass123!")).thenReturn(false);

        var result = changePasswordUseCase.handle(
                new ChangePasswordCommand(USER_ID, USER_ID, "WrongPass123!", "NewStrongPass456!")
        );

        assertThat(result.isFailure()).isTrue();
        verify(userAccountPort, never()).updatePassword(any(), any());
    }

    @Test
    @DisplayName("[US-40] Should soft-deactivate authenticated user's own account")
    void shouldDeactivateOwnAccount() {
        Instant now = Instant.now();
        var deactivatedSnapshot = new ManagedUserSnapshot(
                USER_ID, "user@jouletracker.com", "John", "Doe",
                Set.of("ROLE_USER"), "INACTIVE", true, now
        );
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser(USER_ID)));
        when(userAccountPort.deactivateAccount(eq(USER_ID), any(Instant.class))).thenReturn(deactivatedSnapshot);

        var result = deactivateUserUseCase.handle(new DeactivateUserCommand(USER_ID, USER_ID));

        assertThat(result.isSuccess()).isTrue();
        var output = result.toOptional().orElseThrow();
        assertThat(output.status()).isEqualTo("INACTIVE");
        assertThat(output.deactivatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("[US-40] Should forbid deactivating another user's account")
    void shouldForbidDeactivatingAnotherUser() {
        var result = deactivateUserUseCase.handle(new DeactivateUserCommand(USER_ID, OTHER_USER_ID));

        assertThat(result.isFailure()).isTrue();
        verify(userAccountPort, never()).deactivateAccount(any(), any());
    }

    @Test
    @DisplayName("[US-49] Should allow admin to ban a target user")
    void shouldAllowAdminToBanTargetUser() {
        var bannedSnapshot = new ManagedUserSnapshot(
                USER_ID, "user@jouletracker.com", "John", "Doe",
                Set.of("ROLE_USER"), "BANNED", true, null
        );
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser(USER_ID)));
        when(userAccountPort.updateAccountStatus(eq(USER_ID), eq(AccountModerationStatus.BANNED), any(Instant.class)))
                .thenReturn(bannedSnapshot);

        var result = updateUserStatusUseCase.handle(
                new UpdateUserStatusCommand(USER_ID, ADMIN_ID, "BANNED", "Abuse detected")
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().status()).isEqualTo("BANNED");
    }

    @Test
    @DisplayName("[US-49] Should prevent admin from banning or modifying their own account")
    void shouldPreventAdminSelfModeration() {
        var result = updateUserStatusUseCase.handle(
                new UpdateUserStatusCommand(ADMIN_ID, ADMIN_ID, "BANNED", "Self ban test")
        );

        assertThat(result.isFailure()).isTrue();
        verify(userAccountPort, never()).updateAccountStatus(any(), any(), any());
    }

    @Test
    @DisplayName("[US-49] Should reject invalid status values")
    void shouldRejectInvalidStatusValue() {
        var result = updateUserStatusUseCase.handle(
                new UpdateUserStatusCommand(USER_ID, ADMIN_ID, "UNKNOWN_STATUS", null)
        );

        assertThat(result.isFailure()).isTrue();
        verify(userAccountPort, never()).updateAccountStatus(any(), any(), any());
    }
}
