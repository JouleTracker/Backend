package com.jouletracker.platform.iam.domain.model.valueobjects;

/**
 * Enumeration of security authorization roles available in the platform.
 */
public enum Role {
    /**
     * Standard registered residential / homeowner user.
     */
    ROLE_USER,

    /**
     * Registered MYPE / business account user.
     */
    ROLE_BUSINESS,

    /**
     * Platform administrator with elevated permissions.
     */
    ROLE_ADMIN;

    /**
     * Converts the domain role into the database role representation ("USER", "BUSINESS", "ADMIN").
     *
     * @return database role name
     */
    public String toDbName() {
        return switch (this) {
            case ROLE_ADMIN -> "ADMIN";
            case ROLE_BUSINESS -> "BUSINESS";
            case ROLE_USER -> "USER";
        };
    }

    /**
     * Parses a database role name ("USER", "BUSINESS", "ADMIN" or with "ROLE_" prefix) into a domain {@link Role}.
     *
     * @param dbName database role string
     * @return corresponding Role enum constant
     */
    public static Role fromDbName(String dbName) {
        if ("ADMIN".equalsIgnoreCase(dbName) || "ROLE_ADMIN".equalsIgnoreCase(dbName)) {
            return ROLE_ADMIN;
        }
        if ("BUSINESS".equalsIgnoreCase(dbName) || "ROLE_BUSINESS".equalsIgnoreCase(dbName)) {
            return ROLE_BUSINESS;
        }
        return ROLE_USER;
    }
}
