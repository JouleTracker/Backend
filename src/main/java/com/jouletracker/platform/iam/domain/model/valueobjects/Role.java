package com.jouletracker.platform.iam.domain.model.valueobjects;

/**
 * Enumeration of security authorization roles available in the platform.
 */
public enum Role {
    /**
     * Standard registered platform user.
     */
    ROLE_USER,

    /**
     * Platform administrator with elevated permissions.
     */
    ROLE_ADMIN;

    /**
     * Converts the domain role into the database role representation ("USER", "ADMIN").
     *
     * @return database role name
     */
    public String toDbName() {
        return this == ROLE_ADMIN ? "ADMIN" : "USER";
    }

    /**
     * Parses a database role name ("USER", "ADMIN" or with "ROLE_" prefix) into a domain {@link Role}.
     *
     * @param dbName database role string
     * @return corresponding Role enum constant
     */
    public static Role fromDbName(String dbName) {
        if ("ADMIN".equalsIgnoreCase(dbName) || "ROLE_ADMIN".equalsIgnoreCase(dbName)) {
            return ROLE_ADMIN;
        }
        return ROLE_USER;
    }
}
