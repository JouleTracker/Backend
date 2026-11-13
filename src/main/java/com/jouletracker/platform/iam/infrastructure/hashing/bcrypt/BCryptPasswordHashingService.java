package com.jouletracker.platform.iam.infrastructure.hashing.bcrypt;

import com.jouletracker.platform.iam.domain.model.valueobjects.PasswordHash;
import com.jouletracker.platform.iam.domain.services.PasswordHashingService;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Adapter implementing {@link PasswordHashingService} using Spring Security's {@link PasswordEncoder} (BCrypt).
 */
@Service
@NullMarked
public class BCryptPasswordHashingService implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashingService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        Objects.requireNonNull(rawPassword, "Raw password must not be null");
        return PasswordHash.of(passwordEncoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash) {
        Objects.requireNonNull(rawPassword, "Raw password must not be null");
        Objects.requireNonNull(passwordHash, "PasswordHash must not be null");
        return passwordEncoder.matches(rawPassword, passwordHash.value());
    }
}
