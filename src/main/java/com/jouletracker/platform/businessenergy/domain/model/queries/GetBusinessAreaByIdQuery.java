package com.jouletracker.platform.businessenergy.domain.model.queries;

/**
 * Query to retrieve a single business area by its unique identifier.
 */
public record GetBusinessAreaByIdQuery(
        Long id
) {
}
