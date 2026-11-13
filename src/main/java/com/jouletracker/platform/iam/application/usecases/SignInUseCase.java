package com.jouletracker.platform.iam.application.usecases;

import com.jouletracker.platform.iam.application.commands.SignInCommand;
import com.jouletracker.platform.iam.application.out.AuthenticatedUserResult;
import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;

/**
 * Use case contract for authenticating a user and issuing a JWT access token (TS-02).
 */
@NullMarked
public interface SignInUseCase {

    /**
     * Authenticates credentials, validates email confirmation status, and generates a signed JWT.
     *
     * @param command the sign-in command
     * @return {@link Result} containing {@link AuthenticatedUserResult} on success, or {@link ApplicationError} on failure
     */
    Result<AuthenticatedUserResult, ApplicationError> handle(SignInCommand command);
}
