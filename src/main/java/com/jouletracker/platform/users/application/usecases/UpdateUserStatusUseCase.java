package com.jouletracker.platform.users.application.usecases;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.UpdateUserStatusCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for administrative moderation of user account statuses (US-49).
 */
@NullMarked
public interface UpdateUserStatusUseCase {

    /**
     * Updates the lifecycle/moderation status of a target user account.
     *
     * @param command the admin moderation command
     * @return a {@link Result} containing the updated {@link ManagedUserResult} or an {@link ApplicationError}
     */
    Result<ManagedUserResult, ApplicationError> handle(UpdateUserStatusCommand command);
}
