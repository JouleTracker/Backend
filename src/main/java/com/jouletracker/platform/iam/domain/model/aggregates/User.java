package com.jouletracker.platform.iam.domain.model.aggregates;

import com.jouletracker.platform.iam.domain.model.events.UserRegisteredEvent;
import com.jouletracker.platform.iam.domain.model.events.UserVerifiedEvent;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.jspecify.annotations.NullMarked;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root representing an authenticated user within the IAM bounded context.
 *
 * <p>Extends {@link AbstractDomainAggregateRoot} to enable domain event registration.
 * Encapsulates core identity rules, credentials, authorization roles, account lifecycle status, and verification.</p>
 */
@NullMarked
public class User extends AbstractDomainAggregateRoot<User> {

    private UserId id;
    private final EmailAddress email;
    private PasswordHash passwordHash;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private boolean emailVerified;
    private final Set<Role> roles;

    /**
     * Primary reconstitution constructor used by persistence mappers.
     *
     * @param id            the user's identifier
     * @param email         the user's email address
     * @param passwordHash  the user's hashed password
     * @param firstName     the user's first name
     * @param lastName      the user's last name
     * @param status        the account lifecycle status
     * @param emailVerified whether the user's email address has been verified
     * @param roles         the set of assigned security roles
     */
    public User(
            UserId id,
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            String lastName,
            UserStatus status,
            boolean emailVerified,
            Set<Role> roles) {
        this.id = Objects.requireNonNull(id, "UserId must not be null");
        this.email = Objects.requireNonNull(email, "EmailAddress must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "PasswordHash must not be null");
        this.firstName = validateName(firstName, "First name");
        this.lastName = validateName(lastName, "Last name");
        this.status = Objects.requireNonNull(status, "UserStatus must not be null");
        this.emailVerified = emailVerified;
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Roles must not be null"));
    }

    /**
     * Reconstitution constructor defaulting status from verification flag.
     */
    public User(
            UserId id,
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            String lastName,
            Set<Role> roles,
            boolean emailVerified) {
        this(
                id,
                email,
                passwordHash,
                firstName,
                lastName,
                emailVerified ? UserStatus.ACTIVE : UserStatus.PENDING_VERIFICATION,
                emailVerified,
                roles
        );
    }

    /**
     * Factory method creating a new unverified User aggregate root.
     *
     * @param email        the validated email address
     * @param passwordHash the hashed password
     * @param firstName    the user's first name
     * @param lastName     the user's last name
     * @param roles        the initial roles assigned to the user
     * @return a new unpersisted User aggregate root instance
     */
    public static User create(
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            String lastName,
            Set<Role> roles) {
        return new User(
                UserId.empty(),
                email,
                passwordHash,
                firstName,
                lastName,
                UserStatus.PENDING_VERIFICATION,
                false,
                roles.isEmpty() ? Set.of(Role.ROLE_USER) : roles
        );
    }

    /**
     * Confirms the user's email address and transitions lifecycle status to ACTIVE.
     * Registers a {@link UserVerifiedEvent} if not already verified.
     */
    public void confirmEmail() {
        if (!this.emailVerified) {
            this.emailVerified = true;
            this.status = UserStatus.ACTIVE;
            this.registerDomainEvent(new UserVerifiedEvent(this.id, this.email));
        }
    }

    /**
     * Assigns the persisted identifier to this aggregate upon initial database insert.
     *
     * @param id the persisted identifier
     */
    public void assignId(UserId id) {
        if (this.id.value() != null) {
            throw new IllegalStateException("Cannot reassign an existing user ID");
        }
        this.id = Objects.requireNonNull(id, "UserId must not be null");
        this.registerDomainEvent(new UserRegisteredEvent(this.id, this.email, this.getFullName()));
    }

    /**
     * Updates the password hash.
     *
     * @param newPasswordHash the new password hash
     */
    public void updatePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "New password hash must not be null");
    }

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    private static String validateName(String name, String fieldName) {
        Objects.requireNonNull(name, fieldName + " must not be null");
        String trimmed = name.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    public UserId getId() {
        return id;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
