package com.jouletracker.platform.iam.domain.model.queries;

/**
 * Query to retrieve a user by their unique identifier.
 *
 * @param userId the user's Long ID
 */
public record GetUserByIdQuery(Long userId) {
}
