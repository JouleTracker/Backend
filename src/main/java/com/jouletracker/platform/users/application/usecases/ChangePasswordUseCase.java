package com.jouletracker.platform.users.application.usecases;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.ChangePasswordCommand;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for changing an authenticated user's own password (US-25).
 */
@NullMarked
public interface ChangePasswordUseCase {

    /**
     * Verifies caller authorization and current password before applying a new hashed password.
     *
     * @param command the change-password command
     * @return a {@link Result} containing a confirmation message or an {@link ApplicationError}
     */
    Result<String, ApplicationError> handle(ChangePasswordCommand command);
}
