package com.jouletracker.platform.iam.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Value object representing a unique identifier for a {@link com.jouletracker.platform.iam.domain.model.aggregates.User}.
 *
 * @param value the Long identifier, or null if the aggregate has not yet been persisted
 */
@NullMarked
public record UserId(@Nullable Long value) {

    /**
     * Creates an empty UserId for an unpersisted User aggregate.
     *
     * @return an empty UserId
     */
    public static UserId empty() {
        return new UserId(null);
    }

    /**
     * Creates a UserId with the given Long value.
     *
     * @param value the Long identifier
     * @return a new UserId
     */
    public static UserId of(Long value) {
        Objects.requireNonNull(value, "User id value must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException("User id value must be positive");
        }
        return new UserId(value);
    }

    /**
     * Creates a UserId by parsing a numeric string representation.
     *
     * @param stringValue the string Long representation
     * @return a new UserId
     */
    public static UserId fromString(String stringValue) {
        Objects.requireNonNull(stringValue, "User id string must not be null");
        return UserId.of(Long.valueOf(stringValue.trim()));
    }
}
