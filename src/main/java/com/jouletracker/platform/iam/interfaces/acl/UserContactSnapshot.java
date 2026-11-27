package com.jouletracker.platform.iam.interfaces.acl;

import org.jspecify.annotations.NullMarked;

/**
 * Lightweight ACL snapshot exposing a user's contact details for cross-context notifications (TS-14 / TS-19).
 */
@NullMarked
public record UserContactSnapshot(
        Long userId,
        String email,
        String fullName
) {
}