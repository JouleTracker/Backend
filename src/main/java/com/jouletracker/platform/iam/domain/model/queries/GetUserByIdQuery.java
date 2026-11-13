package com.jouletracker.platform.iam.domain.model.queries;

import java.util.UUID;

/**
 * Query to retrieve a user by their unique identifier.
 *
 * @param userId the user's UUID
 */
public record GetUserByIdQuery(UUID userId) {
}
