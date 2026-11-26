package com.jouletracker.platform.users.application.internal;

import com.jouletracker.platform.shared.application.result.ApplicationError;
import com.jouletracker.platform.shared.application.result.Result;
import com.jouletracker.platform.users.application.commands.UpdateUserProfileCommand;
import com.jouletracker.platform.users.application.out.ManagedUserResult;
import com.jouletracker.platform.users.application.usecases.UpdateUserProfileUseCase;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import com.jouletracker.platform.users.domain.services.ManagedUserAccountPort;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link UpdateUserProfileUseCase} enforcing ownership authorization,
 * account status checks, and personal profile field validation (TS-12 / US-22).
 */
@Service
@NullMarked
public class UpdateUserProfileUseCaseImpl implements UpdateUserProfileUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserProfileUseCaseImpl.class);
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_PHONE_LENGTH = 20;

    private final ManagedUserAccountPort userAccountPort;

    public UpdateUserProfileUseCaseImpl(ManagedUserAccountPort userAccountPort) {
        this.userAccountPort = userAccountPort;
    }

    @Override
    @Transactional
    public Result<ManagedUserResult, ApplicationError> handle(UpdateUserProfileCommand command) {
        if (!command.authenticatedUserId().equals(command.targetUserId())) {
            LOGGER.warn("Profile update denied: authenticated user {} attempted to modify user {}.",
                    command.authenticatedUserId(), command.targetUserId());
            return Result.failure(ApplicationError.forbidden("You are only authorized to update your own profile"));
        }

        Optional<ManagedUserSnapshot> optionalUser = userAccountPort.findById(command.targetUserId());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.targetUserId().toString()));
        }

        ManagedUserSnapshot user = optionalUser.get();
        if (user.isDeactivated() || user.isRestricted()) {
            return Result.failure(ApplicationError.forbidden(
                    "Your account is deactivated or restricted and cannot be updated"
            ));
        }

        String trimmedFirstName = command.firstName().trim();
        if (trimmedFirstName.isBlank() || trimmedFirstName.length() > MAX_NAME_LENGTH) {
            return Result.failure(ApplicationError.validationError(
                    "firstName",
                    "First name must be between 1 and 100 characters"
            ));
        }

        String trimmedLastName = command.lastName().trim();
        if (trimmedLastName.isBlank() || trimmedLastName.length() > MAX_NAME_LENGTH) {
            return Result.failure(ApplicationError.validationError(
                    "lastName",
                    "Last name must be between 1 and 100 characters"
            ));
        }

        @Nullable String normalizedPhone = normalizePhone(command.phone());
        if (normalizedPhone != null && normalizedPhone.length() > MAX_PHONE_LENGTH) {
            return Result.failure(ApplicationError.validationError(
                    "phone",
                    "Phone number must not exceed 20 characters"
            ));
        }

        ManagedUserSnapshot updated = userAccountPort.updateProfile(
                command.targetUserId(),
                trimmedFirstName,
                trimmedLastName,
                normalizedPhone
        );
        LOGGER.info("Profile updated successfully for user {}.", command.targetUserId());
        return Result.success(ManagedUserResult.fromSnapshot(updated));
    }

    private @Nullable String normalizePhone(@Nullable String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
