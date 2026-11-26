package com.jouletracker.platform.users.application.internal;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import com.jouletracker.platform.users.application.commands.ChangePasswordCommand;
import com.jouletracker.platform.users.application.usecases.ChangePasswordUseCase;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import com.jouletracker.platform.users.domain.services.ManagedUserAccountPort;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link ChangePasswordUseCase} enforcing ownership authorization,
 * current password verification via BCrypt, and platform password strength rules (US-25).
 */
@Service
@NullMarked
public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePasswordUseCaseImpl.class);

    private final ManagedUserAccountPort userAccountPort;

    public ChangePasswordUseCaseImpl(ManagedUserAccountPort userAccountPort) {
        this.userAccountPort = userAccountPort;
    }

    @Override
    @Transactional
    public Result<String, ApplicationError> handle(ChangePasswordCommand command) {
        if (!command.authenticatedUserId().equals(command.targetUserId())) {
            LOGGER.warn("Password change denied: authenticated user attempted to modify another user's password.");
            return Result.failure(ApplicationError.forbidden("Solo tienes autorización para cambiar tu propia contraseña"));
        }

        Optional<ManagedUserSnapshot> optionalUser = userAccountPort.findById(command.targetUserId());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.targetUserId().toString()));
        }

        ManagedUserSnapshot user = optionalUser.get();
        if (user.isDeactivated() || user.isRestricted()) {
            return Result.failure(ApplicationError.forbidden("La cuenta está desactivada o restringida y no puede cambiar su contraseña"));
        }

        if (!userAccountPort.verifyPassword(command.targetUserId(), command.currentPassword())) {
            LOGGER.info("Password change rejected: current password verification failed.");
            return Result.failure(ApplicationError.validationError("currentPassword", "La contraseña actual es incorrecta"));
        }

        if (!PasswordPolicy.isSatisfiedBy(command.newPassword())) {
            return Result.failure(ApplicationError.validationError("newPassword", PasswordPolicy.POLICY_MESSAGE));
        }

        if (command.currentPassword().equals(command.newPassword())) {
            return Result.failure(ApplicationError.validationError(
                    "newPassword",
                    "La nueva contraseña debe ser diferente a la contraseña actual"
            ));
        }

        userAccountPort.updatePassword(command.targetUserId(), command.newPassword());
        LOGGER.info("Password changed successfully for user {}.", command.targetUserId());
        return Result.success("La contraseña se actualizó correctamente.");
    }
}
