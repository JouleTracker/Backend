package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.VerifyEmailCommand;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Use case contract for verifying a user's email address using a single-use token.
 */
@NullMarked
public interface VerifyEmailUseCase {

    /**
     * Validates the single-use token, marks it as consumed, and confirms the user's email address.
     *
     * @param command the verify email command
     * @return {@link Result} containing the verified {@link UserResult} on success, or {@link ApplicationError} on failure
     */
    Result<UserResult, ApplicationError> handle(VerifyEmailCommand command);
}
