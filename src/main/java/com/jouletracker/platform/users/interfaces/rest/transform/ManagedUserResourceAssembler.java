package com.jouletracker.platform.users.interfaces.rest.transform;

import com.jouletracker.platform.users.application.out.ManagedUserResult;
import com.jouletracker.platform.users.interfaces.rest.resources.ManagedUserResource;
import org.jspecify.annotations.NullMarked;

/**
 * Assembler transforming {@link ManagedUserResult} into {@link ManagedUserResource}.
 */
@NullMarked
public final class ManagedUserResourceAssembler {

    private ManagedUserResourceAssembler() {
    }

    public static ManagedUserResource toResourceFromResult(ManagedUserResult result) {
        return new ManagedUserResource(
                result.id(),
                result.email(),
                result.firstName(),
                result.lastName(),
                result.phone(),
                result.roles(),
                result.status(),
                result.emailVerified(),
                result.deactivatedAt()
        );
    }
}
