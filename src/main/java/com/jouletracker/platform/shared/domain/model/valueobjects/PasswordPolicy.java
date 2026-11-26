package com.jouletracker.platform.shared.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Shared domain password policy enforcing platform-wide credential strength requirements.
 *
 * <p>Requires 8 to 64 characters including at least one uppercase letter, one lowercase letter,
 * one numeric digit, and one special character.</p>
 */
@NullMarked
public final class PasswordPolicy {

    public static final String POLICY_MESSAGE =
            "Password must be 8-64 characters and include uppercase, lowercase, digit, and special character";

    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+=-])[A-Za-z\\d@$!%*?&#^()_+=-]{8,64}$"
    );

    private PasswordPolicy() {
    }

    /**
     * Checks whether a candidate raw password satisfies the JouleTracker password policy.
     *
     * @param rawPassword the candidate plain-text password
     * @return true if the password complies with all policy constraints, false otherwise
     */
    public static boolean isSatisfiedBy(@Nullable String rawPassword) {
        return rawPassword != null && STRONG_PASSWORD_PATTERN.matcher(rawPassword).matches();
    }
}
