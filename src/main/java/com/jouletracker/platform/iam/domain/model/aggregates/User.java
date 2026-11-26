package com.jouletracker.platform.iam.domain.model.aggregates;

import com.jouletracker.platform.iam.domain.model.events.UserDeactivatedEvent;
import com.jouletracker.platform.iam.domain.model.events.UserPasswordChangedEvent;
import com.jouletracker.platform.iam.domain.model.events.UserRegisteredEvent;
import com.jouletracker.platform.iam.domain.model.events.UserStatusChangedEvent;
import com.jouletracker.platform.iam.domain.model.events.UserVerifiedEvent;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root representing an authenticated user within the IAM bounded context.
 *
 * <p>Extends {@link AbstractDomainAggregateRoot} to enable domain event registration.
 * Encapsulates core identity rules, credentials, authorization roles, account lifecycle status,
 * verification state, and soft-deactivation behavior.</p>
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
    private @Nullable Instant deactivatedAt;
    private final Set<Role> roles;

    /**
     * Full reconstitution constructor used by persistence mappers.
     *
     * @param id            the user's identifier
     * @param email         the user's email address
     * @param passwordHash  the user's hashed password
     * @param firstName     the user's first name
     * @param lastName      the user's last name
     * @param status        the account lifecycle status
     * @param emailVerified whether the user's email address has been verified
     * @param deactivatedAt the timestamp when the account was soft-deactivated, or null
     * @param roles         the set of assigned security roles
     */
    public User(
            UserId id,
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            @Nullable String lastName,
            UserStatus status,
            boolean emailVerified,
            @Nullable Instant deactivatedAt,
            Set<Role> roles) {
        this.id = Objects.requireNonNull(id, "UserId must not be null");
        this.email = Objects.requireNonNull(email, "EmailAddress must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "PasswordHash must not be null");
        this.firstName = validateName(firstName, "First name");
        this.lastName = normalizeOptionalName(lastName);
        this.status = Objects.requireNonNull(status, "UserStatus must not be null");
        this.emailVerified = emailVerified;
        this.deactivatedAt = deactivatedAt;
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Roles must not be null"));
    }

    /**
     * Reconstitution constructor without deactivation timestamp.
     */
    public User(
            UserId id,
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            @Nullable String lastName,
            UserStatus status,
            boolean emailVerified,
            Set<Role> roles) {
        this(id, email, passwordHash, firstName, lastName, status, emailVerified, null, roles);
    }

    /**
     * Reconstitution constructor defaulting status from verification flag.
     */
    public User(
            UserId id,
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            @Nullable String lastName,
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
                null,
                roles
        );
    }

    /**
     * Factory method creating a new unverified User aggregate root.
     *
     * @param email        the validated email address
     * @param passwordHash the hashed password
     * @param firstName    the user's first name
     * @param lastName     the user's optional last name
     * @param roles        the initial roles assigned to the user
     * @return a new unpersisted User aggregate root instance
     */
    public static User create(
            EmailAddress email,
            PasswordHash passwordHash,
            String firstName,
            @Nullable String lastName,
            Set<Role> roles) {
        return new User(
                UserId.empty(),
                email,
                passwordHash,
                firstName,
                lastName,
                UserStatus.PENDING_VERIFICATION,
                false,
                null,
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
            this.deactivatedAt = null;
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
     * Updates the user's password hash via domain behavior and registers {@link UserPasswordChangedEvent}.
     *
     * @param newPasswordHash the new securely hashed password
     */
    public void updatePassword(PasswordHash newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash, "New password hash must not be null");
        if (this.id.value() != null) {
            this.registerDomainEvent(new UserPasswordChangedEvent(this.id, this.email));
        }
    }

    /**
     * Soft-deactivates the user account, recording the deactivation timestamp and transitioning
     * status to {@link UserStatus#INACTIVE}.
     *
     * @param deactivatedAt the UTC timestamp of deactivation
     * @throws IllegalStateException if the account is already deactivated or banned
     */
    public void deactivate(Instant deactivatedAt) {
        Objects.requireNonNull(deactivatedAt, "Deactivation timestamp must not be null");
        if (this.status == UserStatus.INACTIVE) {
            throw new IllegalStateException("User account is already deactivated");
        }
        if (this.status == UserStatus.BANNED) {
            throw new IllegalStateException("Banned user account cannot be self-deactivated");
        }
        this.status = UserStatus.INACTIVE;
        this.deactivatedAt = deactivatedAt;
        if (this.id.value() != null) {
            this.registerDomainEvent(new UserDeactivatedEvent(this.id, this.email, deactivatedAt));
        }
    }

    /**
     * Updates the user's moderation/lifecycle status via domain behavior.
     *
     * @param newStatus the target {@link UserStatus}
     * @param timestamp the UTC timestamp of the status change
     */
    public void updateStatus(UserStatus newStatus, Instant timestamp) {
        Objects.requireNonNull(newStatus, "Target UserStatus must not be null");
        Objects.requireNonNull(timestamp, "Status change timestamp must not be null");

        UserStatus previousStatus = this.status;
        if (previousStatus == newStatus) {
            return;
        }

        this.status = newStatus;
        if (newStatus == UserStatus.ACTIVE) {
            this.deactivatedAt = null;
        } else if (newStatus == UserStatus.INACTIVE) {
            this.deactivatedAt = timestamp;
        }

        if (this.id.value() != null) {
            this.registerDomainEvent(new UserStatusChangedEvent(
                    this.id,
                    this.email,
                    previousStatus,
                    newStatus,
                    timestamp
            ));
        }
    }

    /**
     * Checks whether the account is currently eligible to authenticate and access protected resources.
     *
     * @return true if email is verified and status is ACTIVE
     */
    public boolean canAuthenticate() {
        return this.emailVerified && this.status == UserStatus.ACTIVE;
    }

    /**
     * Checks whether the account has been soft-deactivated.
     *
     * @return true if status is INACTIVE
     */
    public boolean isDeactivated() {
        return this.status == UserStatus.INACTIVE;
    }

    /**
     * Checks whether the account is banned or suspended by moderation.
     *
     * @return true if status is BANNED or SUSPENDED
     */
    public boolean isRestricted() {
        return this.status == UserStatus.BANNED || this.status == UserStatus.SUSPENDED;
    }

    /**
     * Checks whether this user holds the administrator role.
     *
     * @return true if {@link Role#ROLE_ADMIN} is assigned
     */
    public boolean isAdmin() {
        return this.roles.contains(Role.ROLE_ADMIN);
    }

    public String getFullName() {
        if (lastName.isBlank() || firstName.equalsIgnoreCase(lastName)) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private static String validateName(String name, String fieldName) {
        Objects.requireNonNull(name, fieldName + " must not be null");
        String trimmed = name.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static String normalizeOptionalName(@Nullable String name) {
        return name == null ? "" : name.trim();
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

    public @Nullable Instant getDeactivatedAt() {
        return deactivatedAt;
    }
}
