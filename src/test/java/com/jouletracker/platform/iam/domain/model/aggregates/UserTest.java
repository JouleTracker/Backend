package com.jouletracker.platform.iam.domain.model.aggregates;

import com.jouletracker.platform.iam.domain.model.events.UserDeactivatedEvent;
import com.jouletracker.platform.iam.domain.model.events.UserRegisteredEvent;
import com.jouletracker.platform.iam.domain.model.events.UserStatusChangedEvent;
import com.jouletracker.platform.iam.domain.model.events.UserVerifiedEvent;
import com.jouletracker.platform.iam.domain.model.valueobjects.EmailAddress;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Aggregate Root Unit Tests")
class UserTest {

    private static final Long TEST_USER_ID = 1L;

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
        assertThat(user.canAuthenticate()).isTrue();
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

        Long secondId = 2L;
        assertThatThrownBy(() -> user.assignId(UserId.of(secondId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Should soft-deactivate active user, record timestamp, block authentication, and raise event")
    void shouldDeactivateUserViaDomainBehavior() {
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("active@jouletracker.com"),
                PasswordHash.of("$2a$12$hash"),
                "Jane",
                "Doe",
                UserStatus.ACTIVE,
                true,
                Set.of(Role.ROLE_USER)
        );
        Instant now = Instant.now();

        user.deactivate(now);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getDeactivatedAt()).isEqualTo(now);
        assertThat(user.isDeactivated()).isTrue();
        assertThat(user.canAuthenticate()).isFalse();
        assertThat(user.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(UserDeactivatedEvent.class);
    }

    @Test
    @DisplayName("Should ban and reactivate user via domain moderation status transition")
    void shouldUpdateModerationStatusViaDomainBehavior() {
        var user = new User(
                UserId.of(TEST_USER_ID),
                EmailAddress.of("member@jouletracker.com"),
                PasswordHash.of("$2a$12$hash"),
                "Jane",
                "Doe",
                UserStatus.ACTIVE,
                true,
                Set.of(Role.ROLE_USER)
        );
        Instant now = Instant.now();

        user.updateStatus(UserStatus.BANNED, now);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        assertThat(user.isRestricted()).isTrue();
        assertThat(user.canAuthenticate()).isFalse();
        assertThat(user.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(UserStatusChangedEvent.class);

        user.clearDomainEvents();
        user.updateStatus(UserStatus.ACTIVE, now.plusSeconds(60));
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getDeactivatedAt()).isNull();
        assertThat(user.canAuthenticate()).isTrue();
    }

    @Test
    @DisplayName("Should allow blank or null lastName for single-word names and deduplicate identical names")
    void shouldAllowOptionalLastNameAndDeduplicateFullName() {
        var email = EmailAddress.of("rodrigo@jouletracker.com");
        var hash = PasswordHash.of("$2a$12$e8x6...dummyhash");

        var blankLastNameUser = User.create(email, hash, "rodrigo", "", Set.of(Role.ROLE_USER));
        assertThat(blankLastNameUser.getFirstName()).isEqualTo("rodrigo");
        assertThat(blankLastNameUser.getLastName()).isEmpty();
        assertThat(blankLastNameUser.getFullName()).isEqualTo("rodrigo");

        var nullLastNameUser = User.create(email, hash, "rodrigo", null, Set.of(Role.ROLE_USER));
        assertThat(nullLastNameUser.getLastName()).isEmpty();
        assertThat(nullLastNameUser.getFullName()).isEqualTo("rodrigo");

        var duplicateNameUser = User.create(email, hash, "rodrigo", "RODRIGO", Set.of(Role.ROLE_USER));
        assertThat(duplicateNameUser.getFullName()).isEqualTo("rodrigo");
    }
}
