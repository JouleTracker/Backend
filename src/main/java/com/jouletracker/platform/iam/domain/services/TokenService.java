package com.jouletracker.platform.iam.domain.services;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Domain service contract for JSON Web Token (JWT) generation, validation, and claim parsing.
 */
@NullMarked
public interface TokenService {

    /**
     * Generates a signed JWT access token for the given authenticated user.
     *
     * @param user the authenticated user aggregate
     * @return the encoded, signed JWT token string
     */
    String generateToken(User user);

    /**
     * Validates whether the given JWT string has a valid signature and has not expired.
     *
     * @param token the encoded JWT string
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Extracts the username (email) subject from a valid token.
     *
     * @param token the encoded JWT string
     * @return the subject email
     */
    String extractUsername(String token);

    /**
     * Extracts the user ID claim from a valid token.
     *
     * @param token the encoded JWT string
     * @return the UUID user ID, or null if not found
     */
    @Nullable UUID extractUserId(String token);

    /**
     * Extracts the list of granted authority roles from a valid token.
     *
     * @param token the encoded JWT string
     * @return list of role strings (e.g. ["ROLE_USER"])
     */
    List<String> extractRoles(String token);
}
