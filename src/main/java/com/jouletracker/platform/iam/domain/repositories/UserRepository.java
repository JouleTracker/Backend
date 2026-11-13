package com.jouletracker.platform.iam.domain.repositories;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for managing {@link User} aggregate persistence.
 *
 * <p>Free of JPA, Spring Data, and database-specific dependencies.</p>
 */
@NullMarked
public interface UserRepository {

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the user ID
     * @return an Optional containing the User if found, or empty otherwise
     */
    Optional<User> findById(UserId id);

    /**
     * Returns all registered users.
     *
     * @return list of all User aggregates
     */
    List<User> findAll();

    /**
     * Finds a user by their email address.
     *
     * @param email the user email address
     * @return an Optional containing the User if found, or empty otherwise
     */
    Optional<User> findByEmail(EmailAddress email);

    /**
     * Checks whether an account with the given email address already exists.
     *
     * @param email the email address to check
     * @return true if a user with this email already exists, false otherwise
     */
    boolean existsByEmail(EmailAddress email);

    /**
     * Persists or updates the given user aggregate.
     *
     * @param user the user aggregate to persist
     * @return the persisted User with its identity assigned
     */
    User save(User user);
}
