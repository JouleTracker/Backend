package com.jouletracker.platform.users.application.usecases;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.DeactivateUserCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for soft-deactivating an authenticated user's account (US-40).
 */
@NullMarked
public interface DeactivateUserUseCase {

    /**
     * Soft-deactivates the specified user account via domain behavior.
     *
     * @param command the deactivate-user command
     * @return a {@link Result} containing the updated {@link ManagedUserResult} or an {@link ApplicationError}
     */
    Result<ManagedUserResult, ApplicationError> handle(DeactivateUserCommand command);
}
