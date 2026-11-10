package com.jouletracker.platform.businessenergy.domain.model.queries;

/**
 * Query to retrieve business areas belonging to a user.
 */
public record GetAllBusinessAreasQuery(
        Long userId
) {
}
