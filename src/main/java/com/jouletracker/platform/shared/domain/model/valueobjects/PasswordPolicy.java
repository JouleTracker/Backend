package com.jouletracker.platform.shared.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Shared domain password policy enforcing platform-wide credential strength requirements.
 *
 * <p>Requires 8 to 128 characters including at least one uppercase letter, one lowercase letter,
 * one numeric digit, and one printable special character (any non-alphanumeric, non-whitespace character).</p>
 */
@NullMarked
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 128;

    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,128}$";

    public static final String POLICY_MESSAGE =
            "La contraseña debe tener entre 8 y 128 caracteres e incluir al menos una letra mayúscula, una letra minúscula, un número y un carácter especial.";

    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

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
