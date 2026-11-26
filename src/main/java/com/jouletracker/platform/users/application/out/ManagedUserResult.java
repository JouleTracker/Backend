package com.jouletracker.platform.users.application.out;

import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Set;

/**
 * Application output projection representing a user account managed within the User Management context.
 */
@NullMarked
public record ManagedUserResult(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        String status,
        boolean emailVerified,
        @Nullable Instant deactivatedAt) {

    public static ManagedUserResult fromSnapshot(ManagedUserSnapshot snapshot) {
        return new ManagedUserResult(
                snapshot.id(),
                snapshot.email(),
                snapshot.firstName(),
                snapshot.lastName(),
                snapshot.roles(),
                snapshot.status(),
                snapshot.emailVerified(),
                snapshot.deactivatedAt()
        );
    }
}
