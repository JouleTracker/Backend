package com.jouletracker.platform.iam.domain.model.events;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * Domain event raised when a user account is soft-deactivated.
 *
 * @param userId        the user identifier
 * @param email         the user's email address
 * @param deactivatedAt the UTC timestamp when deactivation occurred
 */
@NullMarked
public record UserDeactivatedEvent(UserId userId, EmailAddress email, Instant deactivatedAt) {
}
