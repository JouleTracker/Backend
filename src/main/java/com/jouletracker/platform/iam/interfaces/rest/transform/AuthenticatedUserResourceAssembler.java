package com.jouletracker.platform.iam.interfaces.rest.transform;

import com.jouletracker.platform.iam.application.out.AuthenticatedUserResult;
import com.jouletracker.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import org.jspecify.annotations.NullMarked;

/**
 * Assembler transforming {@link AuthenticatedUserResult} into {@link AuthenticatedUserResource}.
 */
@NullMarked
public final class AuthenticatedUserResourceAssembler {

    private AuthenticatedUserResourceAssembler() {
    }

    /**
     * Converts an authenticated user application result into a public REST response.
     *
     * @param result the authenticated user result
     * @return the assembled AuthenticatedUserResource
     */
    public static AuthenticatedUserResource toResourceFromAuthenticatedUserResult(AuthenticatedUserResult result) {
        return new AuthenticatedUserResource(
                result.token(),
                result.tokenType(),
                UserResourceAssembler.toResourceFromUserResult(result.user())
        );
    }
}
