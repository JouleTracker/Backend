package com.jouletracker.platform.iam.domain.model.events;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

/**
 * Domain event raised when a user's account lifecycle status is updated by moderation.
 *
 * @param userId         the affected user identifier
 * @param email          the affected user's email address
 * @param previousStatus the status before the change
 * @param newStatus      the status after the change
 * @param changedAt      the UTC timestamp of the moderation action
 */
@NullMarked
public record UserStatusChangedEvent(
        UserId userId,
        EmailAddress email,
        UserStatus previousStatus,
        UserStatus newStatus,
        Instant changedAt) {
}
