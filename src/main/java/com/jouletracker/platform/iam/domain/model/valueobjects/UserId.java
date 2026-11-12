package com.jouletracker.platform.iam.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier for a {@link com.jouletracker.platform.iam.domain.model.aggregates.User}.
 *
 * @param value the UUID identifier, or null if the aggregate has not yet been persisted
 */
@NullMarked
public record UserId(@Nullable UUID value) {

    /**
     * Creates an empty UserId for an unpersisted User aggregate.
     *
     * @return an empty UserId
     */
    public static UserId empty() {
        return new UserId(null);
    }

    /**
     * Creates a UserId with the given UUID value.
     *
     * @param value the UUID identifier
     * @return a new UserId
     */
    public static UserId of(UUID value) {
        Objects.requireNonNull(value, "User id value must not be null");
        return new UserId(value);
    }

    /**
     * Creates a new random UserId.
     *
     * @return a new UserId with a random UUID
     */
    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }

    /**
     * Creates a UserId by parsing a UUID string representation.
     *
     * @param stringValue the string UUID representation
     * @return a new UserId
     */
    public static UserId fromString(String stringValue) {
        Objects.requireNonNull(stringValue, "User id string must not be null");
        return new UserId(UUID.fromString(stringValue));
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }
}
