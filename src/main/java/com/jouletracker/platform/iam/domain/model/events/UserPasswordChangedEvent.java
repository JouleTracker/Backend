package com.jouletracker.platform.iam.domain.model.events;

import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import org.jspecify.annotations.NullMarked;

/**
 * Domain event raised when a user's password hash is updated (via password change or recovery).
 *
 * @param userId the user identifier
 * @param email  the user's email address
 */
@NullMarked
public record UserPasswordChangedEvent(UserId userId, EmailAddress email) {
}
