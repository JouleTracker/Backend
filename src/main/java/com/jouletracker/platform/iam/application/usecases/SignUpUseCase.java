package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.SignUpCommand;
import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Use case contract for registering a new user account (TS-01).
 */
@NullMarked
public interface SignUpUseCase {

    /**
     * Executes user registration, password hashing, persistence, and verification email dispatch.
     *
     * @param command the registration command
     * @return {@link Result} containing the created {@link UserResult} on success, or {@link ApplicationError} on failure
     */
    Result<UserResult, ApplicationError> handle(SignUpCommand command);
}
