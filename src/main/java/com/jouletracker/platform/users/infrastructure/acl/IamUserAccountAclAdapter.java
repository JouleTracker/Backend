package com.jouletracker.platform.users.infrastructure.acl;

import com.jouletracker.platform.iam.domain.model.aggregates.User;
import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.model.valueobjects.Role;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserId;
import com.jouletracker.platform.iam.domain.model.valueobjects.UserStatus;
import com.jouletracker.platform.iam.domain.repositories.UserRepository;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import com.jouletracker.platform.users.domain.model.valueobjects.AccountModerationStatus;
import com.jouletracker.platform.users.domain.model.valueobjects.ManagedUserSnapshot;
import com.jouletracker.platform.users.domain.services.ManagedUserAccountPort;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Anti-Corruption Layer (ACL) adapter bridging the User Management bounded context
 * to the IAM bounded context's {@link User} aggregate and cryptographic services.
 *
 * <p>All state transitions invoke domain behavior methods on the {@link User} aggregate root
 * rather than raw setters or direct persistence entity mutation.</p>
 */
@Component
@NullMarked
public class IamUserAccountAclAdapter implements ManagedUserAccountPort {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;

    public IamUserAccountAclAdapter(
            UserRepository userRepository,
            PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ManagedUserSnapshot> findById(UUID userId) {
        return userRepository.findById(UserId.of(userId)).map(this::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassword(UUID userId, String rawPassword) {
        return userRepository.findById(UserId.of(userId))
                .map(user -> passwordHashingService.matches(rawPassword, user.getPasswordHash()))
                .orElse(false);
    }

    @Override
    @Transactional
    public ManagedUserSnapshot updatePassword(UUID userId, String newRawPassword) {
        User user = requireUser(userId);
        PasswordHash newHash = passwordHashingService.hash(newRawPassword);
        user.updatePassword(newHash);
        User savedUser = userRepository.save(user);
        return toSnapshot(savedUser);
    }

    @Override
    @Transactional
    public ManagedUserSnapshot deactivateAccount(UUID userId, Instant deactivatedAt) {
        User user = requireUser(userId);
        user.deactivate(deactivatedAt);
        User savedUser = userRepository.save(user);
        return toSnapshot(savedUser);
    }

    @Override
    @Transactional
    public ManagedUserSnapshot updateAccountStatus(UUID userId, AccountModerationStatus targetStatus, Instant timestamp) {
        User user = requireUser(userId);
        UserStatus mappedStatus = switch (targetStatus) {
            case ACTIVE -> UserStatus.ACTIVE;
            case SUSPENDED -> UserStatus.SUSPENDED;
            case BANNED -> UserStatus.BANNED;
            case INACTIVE -> UserStatus.INACTIVE;
        };
        user.updateStatus(mappedStatus, timestamp);
        User savedUser = userRepository.save(user);
        return toSnapshot(savedUser);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new IllegalStateException("User not found for ID: " + userId));
    }

    private ManagedUserSnapshot toSnapshot(User user) {
        UUID id = Objects.requireNonNull(user.getId().value(), "Persisted User must have a non-null ID");
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toUnmodifiableSet());
        return new ManagedUserSnapshot(
                id,
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                roleNames,
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getDeactivatedAt()
        );
    }
}
