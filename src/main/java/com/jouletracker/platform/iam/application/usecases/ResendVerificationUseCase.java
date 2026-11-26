package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.ResendVerificationCommand;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound port for issuing a new email verification token to an unverified user account (US-04).
 */
@NullMarked
public interface ResendVerificationUseCase {

    /**
     * Invalidates prior verification tokens and dispatches a fresh verification email if unverified.
     *
     * @param command the resend-verification command
     * @return a {@link Result} containing a confirmation message or an {@link ApplicationError}
     */
    Result<String, ApplicationError> handle(ResendVerificationCommand command);
}
