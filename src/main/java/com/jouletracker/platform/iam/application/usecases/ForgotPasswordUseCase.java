package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.ForgotPasswordCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for initiating password recovery and dispatching a reset link via email (TS-03).
 */
@NullMarked
public interface ForgotPasswordUseCase {

    /**
     * Generates a single-use, expiring password recovery token and emails the reset link if eligible.
     *
     * @param command the forgot-password command
     * @return a {@link Result} containing an informational message or an {@link ApplicationError}
     */
    Result<String, ApplicationError> handle(ForgotPasswordCommand command);
}
