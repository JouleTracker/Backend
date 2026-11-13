package com.jouletracker.platform.iam.application.out;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import org.jspecify.annotations.NullMarked;

/**
 * Application output record representing the outcome of a successful user authentication.
 *
 * @param token     the signed JWT access token
 * @param tokenType the authentication scheme (Bearer)
 * @param user      the authenticated user projection
 */
@NullMarked
public record AuthenticatedUserResult(
        String token,
        String tokenType,
        UserResult user) {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    /**
     * Factory method creating an {@link AuthenticatedUserResult} with Bearer scheme.
     *
     * @param token the signed JWT access token
     * @param user  the domain aggregate root
     * @return the assembled AuthenticatedUserResult
     */
    public static AuthenticatedUserResult of(String token, User user) {
        return new AuthenticatedUserResult(token, BEARER_TOKEN_TYPE, UserResult.fromUser(user));
    }
}
