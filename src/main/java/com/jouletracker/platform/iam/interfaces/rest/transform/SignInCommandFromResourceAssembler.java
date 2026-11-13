package com.jouletracker.platform.iam.interfaces.rest.transform;

import com.jouletracker.platform.iam.application.commands.SignInCommand;
import com.jouletracker.platform.iam.interfaces.rest.resources.SignInRequest;
import org.jspecify.annotations.NullMarked;

/**
 * Assembler transforming {@link SignInRequest} into {@link SignInCommand}.
 */
@NullMarked
public final class SignInCommandFromResourceAssembler {

    private SignInCommandFromResourceAssembler() {
    }

    /**
     * Converts a REST sign-in request into an application command.
     *
     * @param resource the sign-in request resource
     * @return the assembled SignInCommand
     */
    public static SignInCommand toCommandFromResource(SignInRequest resource) {
        return new SignInCommand(
                resource.email(),
                resource.password()
        );
    }
}
