package com.jouletracker.platform.iam.interfaces.rest.transform;

import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.interfaces.rest.resources.UserResource;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Assembler transforming {@link UserResult} into {@link UserResource}.
 */
@NullMarked
public final class UserResourceAssembler {

    private UserResourceAssembler() {
    }

    /**
     * Converts an application user result into a public REST resource.
     *
     * @param result the application output
     * @return the assembled UserResource
     */
    public static UserResource toResourceFromUserResult(UserResult result) {
        Set<String> roles = result.roles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        return new UserResource(
                result.id(),
                result.email(),
                result.firstName(),
                result.lastName(),
                roles,
                result.emailVerified()
        );
    }
}
