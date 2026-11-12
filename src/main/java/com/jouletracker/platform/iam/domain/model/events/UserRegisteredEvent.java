package com.jouletracker.platform.iam.domain.model.events;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * Domain event dispatched when a new user completes the initial registration process.
 *
 * @param userId     the identifier of the registered user
 * @param email      the registered email address
 * @param fullName   the full name of the registered user
 * @param occurredOn the timestamp when the event occurred
 */
@NullMarked
public record UserRegisteredEvent(
        UserId userId,
        EmailAddress email,
        String fullName,
        Instant occurredOn) {

    /**
     * Factory constructor defaulting {@code occurredOn} to the current instant.
     */
    public UserRegisteredEvent(UserId userId, EmailAddress email, String fullName) {
        this(userId, email, fullName, Instant.now());
    }
}
