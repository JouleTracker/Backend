package com.jouletracker.platform.iam.interfaces.rest.transform;

import com.jouletracker.platform.iam.application.commands.SignUpCommand;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignUpRequest;
import org.jspecify.annotations.NullMarked;

/**
 * Assembler transforming {@link SignUpRequest} into {@link SignUpCommand}.
 */
@NullMarked
public final class SignUpCommandFromResourceAssembler {

    private SignUpCommandFromResourceAssembler() {
    }

    /**
     * Converts a REST request payload into an application command.
     *
     * @param resource the sign-up request resource
     * @return the assembled SignUpCommand
     */
    public static SignUpCommand toCommandFromResource(SignUpRequest resource) {
        String normalizedLastName = resource.lastName() != null ? resource.lastName().trim() : "";
        return new SignUpCommand(
                resource.email(),
                resource.password(),
                resource.firstName(),
                normalizedLastName
        );
    }
}
