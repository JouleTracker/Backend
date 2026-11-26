package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.ResetPasswordCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for validating a password reset token and updating the user's password hash (TS-03).
 */
@NullMarked
public interface ResetPasswordUseCase {

    /**
     * Validates token existence, expiration, and single-use status, then updates the user's password.
     *
     * @param command the reset-password command
     * @return a {@link Result} containing a confirmation message or an {@link ApplicationError}
     */
    Result<String, ApplicationError> handle(ResetPasswordCommand command);
}
