package com.jouletracker.platform.iam.domain.model.events;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * Domain event dispatched when a user successfully verifies their email address.
 *
 * @param userId     the identifier of the verified user
 * @param email      the verified email address
 * @param occurredOn the timestamp when the event occurred
 */
@NullMarked
public record UserVerifiedEvent(
        UserId userId,
        EmailAddress email,
        Instant occurredOn) {

    /**
     * Factory constructor defaulting {@code occurredOn} to the current instant.
     */
    public UserVerifiedEvent(UserId userId, EmailAddress email) {
        this(userId, email, Instant.now());
    }
}
