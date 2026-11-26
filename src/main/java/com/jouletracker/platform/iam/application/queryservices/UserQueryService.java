package com.jouletracker.platform.iam.application.queryservices;

import com.jouletracker.platform.iam.application.out.UserResult;
import com.jouletracker.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.jouletracker.platform.iam.domain.model.queries.GetUserByIdQuery;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

/**
 * Application query service contract for reading user profiles.
 */
@NullMarked
public interface UserQueryService {

    /**
     * Retrieves all registered users.
     *
     * @param query the query object
     * @return list of user results
     */
    List<UserResult> handle(GetAllUsersQuery query);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param query the query object containing the user UUID
     * @return an Optional containing the UserResult if found
     */
    Optional<UserResult> handle(GetUserByIdQuery query);
}
