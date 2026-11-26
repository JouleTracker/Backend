package com.jouletracker.platform.users.application.internal;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.DeactivateUserCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import com.jouletracker.platform.users.application.usecases.DeactivateUserUseCase;
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
 * Implementation of {@link DeactivateUserUseCase} performing soft-deactivation through
 * domain behavior on the User aggregate (US-40).
 */
@Service
@NullMarked
public class DeactivateUserUseCaseImpl implements DeactivateUserUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivateUserUseCaseImpl.class);

    private final ManagedUserAccountPort userAccountPort;

    public DeactivateUserUseCaseImpl(ManagedUserAccountPort userAccountPort) {
        this.userAccountPort = userAccountPort;
    }

    @Override
    @Transactional
    public Result<ManagedUserResult, ApplicationError> handle(DeactivateUserCommand command) {
        if (!command.authenticatedUserId().equals(command.targetUserId())) {
            LOGGER.warn("Account deactivation denied: caller attempted to deactivate another user's account.");
            return Result.failure(ApplicationError.forbidden("You are only authorized to deactivate your own account"));
        }

        Optional<ManagedUserSnapshot> optionalUser = userAccountPort.findById(command.targetUserId());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.targetUserId().toString()));
        }

        ManagedUserSnapshot user = optionalUser.get();
        if (user.isDeactivated()) {
            return Result.failure(ApplicationError.conflict("User", "Account is already deactivated"));
        }
        if (user.isBanned()) {
            return Result.failure(ApplicationError.forbidden("Banned accounts cannot be self-deactivated"));
        }

        ManagedUserSnapshot deactivated = userAccountPort.deactivateAccount(command.targetUserId(), Instant.now());
        LOGGER.info("User account {} soft-deactivated at {}.", deactivated.id(), deactivated.deactivatedAt());
        return Result.success(ManagedUserResult.fromSnapshot(deactivated));
    }
}
