package com.jouletracker.platform.iam.domain.model.valueobjects;

import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a validated email address.
 *
 * <p>Enforces RFC-compliant email formatting and normalizes value to lower-case.</p>
 *
 * @param value the normalized email string
 */
@NullMarked
public record EmailAddress(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Canonical constructor validating and normalizing the email address.
     *
     * @param value the raw email string
     * @throws IllegalArgumentException if email is null, blank, or malformed
     */
    public EmailAddress {
        Objects.requireNonNull(value, "Email address must not be null");
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Email address must not be blank");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + trimmed);
        }
        value = trimmed.toLowerCase(Locale.ROOT);
    }

    /**
     * Factory method creating a validated {@link EmailAddress}.
     *
     * @param value the email string
     * @return a validated EmailAddress instance
     */
    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
