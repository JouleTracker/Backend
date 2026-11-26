package com.jouletracker.platform.users.application.usecases;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.UpdateUserProfileCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import org.jspecify.annotations.NullMarked;

/**
 * Inbound use case port for updating an authenticated user's profile information (TS-12 / US-22).
 */
@NullMarked
public interface UpdateUserProfileUseCase {

    /**
     * Handles a user profile update request enforcing ownership and input validation.
     *
     * @param command the profile update command
     * @return Result containing updated {@link ManagedUserResult} or {@link ApplicationError}
     */
    Result<ManagedUserResult, ApplicationError> handle(UpdateUserProfileCommand command);
}
