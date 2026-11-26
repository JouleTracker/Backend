package com.jouletracker.platform.users.application.internal;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.UpdateUserStatusCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import com.jouletracker.platform.users.application.usecases.UpdateUserStatusUseCase;
import com.jouletracker.platform.users.domain.model.valueobjects.AccountModerationStatus;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import com.jouletracker.platform.users.domain.services.ManagedUserAccountPort;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementation of {@link UpdateUserStatusUseCase} handling administrative user moderation
 * such as banning, suspending, deactivating, or reactivating accounts (US-49).
 */
@Service
@NullMarked
public class UpdateUserStatusUseCaseImpl implements UpdateUserStatusUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserStatusUseCaseImpl.class);

    private final ManagedUserAccountPort userAccountPort;

    public UpdateUserStatusUseCaseImpl(ManagedUserAccountPort userAccountPort) {
        this.userAccountPort = userAccountPort;
    }

    @Override
    @Transactional
    public Result<ManagedUserResult, ApplicationError> handle(UpdateUserStatusCommand command) {
        AccountModerationStatus targetStatus;
        try {
            targetStatus = AccountModerationStatus.fromString(command.targetStatus());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("status", ex.getMessage()));
        }

        if (command.adminUserId().equals(command.targetUserId())) {
            LOGGER.warn("Admin {} attempted to modify their own account status to {}.", command.adminUserId(), targetStatus);
            return Result.failure(ApplicationError.forbidden(
                    "Los administradores no pueden bloquear, suspender ni modificar el estado de su propia cuenta"
            ));
        }

        Optional<ManagedUserSnapshot> optionalUser = userAccountPort.findById(command.targetUserId());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.targetUserId().toString()));
        }

        ManagedUserSnapshot updated = userAccountPort.updateAccountStatus(
                command.targetUserId(),
                targetStatus,
                Instant.now()
        );

        LOGGER.info(
                "Admin {} updated status of user {} to {} (reason: {}).",
                command.adminUserId(),
                command.targetUserId(),
                targetStatus,
                command.reason() != null ? command.reason() : "N/A"
        );

        return Result.success(ManagedUserResult.fromSnapshot(updated));
    }
}
