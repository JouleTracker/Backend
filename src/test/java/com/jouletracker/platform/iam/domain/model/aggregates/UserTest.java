package com.jouletracker.platform.iam.domain.model.aggregates;

import com.jouletracker.platform.iam.domain.model.events.UserRegisteredEvent;
import com.jouletracker.platform.iam.domain.model.events.UserVerifiedEvent;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Aggregate Root Unit Tests")
class UserTest {

    private static final UUID TEST_USER_ID = UUID.fromString("67417618-dd5e-11f1-a688-50ebf6e6183d");

    @Test
    @DisplayName("Should create unverified user and register UserRegisteredEvent after persistence ID assignment")
    void shouldCreateUserWithUnverifiedEmailAndRegisterEvent() {
        var email = EmailAddress.of("test@jouletracker.com");
        var hash = PasswordHash.of("$2a$12$e8x6...dummyhash");

        var user = User.create(email, hash, "Jane", "Doe", Set.of(Role.ROLE_USER));

        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.getFullName()).isEqualTo("Jane Doe");
        assertThat(user.getRoles()).containsExactly(Role.ROLE_USER);

        assertThat(user.domainEvents()).isEmpty();
        user.assignId(UserId.of(TEST_USER_ID));

        assertThat(user.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(UserRegisteredEvent.class);
    }

    @Test
    @DisplayName("Should confirm email, update status to ACTIVE, and register UserVerifiedEvent")
    void shouldConfirmEmailAndRegisterEvent() {
        var email = EmailAddress.of("test@jouletracker.com");
        var hash = PasswordHash.of("$2a$12$e8x6...dummyhash");

        var user = User.create(email, hash, "Jane", "Doe", Set.of(Role.ROLE_USER));
        user.assignId(UserId.of(TEST_USER_ID));
        user.clearDomainEvents();
        user.confirmEmail();

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(UserVerifiedEvent.class);
    }

    @Test
    @DisplayName("Should not register duplicate UserVerifiedEvent when confirmed again")
    void shouldNotDuplicateVerifiedEvent() {
        var email = EmailAddress.of("test@jouletracker.com");
        var hash = PasswordHash.of("$2a$12$e8x6...dummyhash");

        var user = User.create(email, hash, "Jane", "Doe", Set.of(Role.ROLE_USER));
        user.assignId(UserId.of(TEST_USER_ID));
        user.clearDomainEvents();
        user.confirmEmail();
        user.clearDomainEvents();

        user.confirmEmail();

        assertThat(user.domainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Should assign ID only once")
    void shouldAssignIdOnce() {
        var email = EmailAddress.of("test@jouletracker.com");
        var hash = PasswordHash.of("$2a$12$e8x6...dummyhash");

        var user = User.create(email, hash, "Jane", "Doe", Set.of(Role.ROLE_USER));
        user.assignId(UserId.of(TEST_USER_ID));
        assertThat(user.getId().value()).isEqualTo(TEST_USER_ID);

        UUID secondId = UUID.randomUUID();
        assertThatThrownBy(() -> user.assignId(UserId.of(secondId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
