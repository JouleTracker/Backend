
package com.jouletracker.platform.businessenergy.domain.model.aggregates;

import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Aggregate Root representing a physical area of an MYPE business.
 * Each area belongs to one user and can be used to organize energy monitoring.
 * US-32: Supports shared notification recipients by business area.
 */
@Getter
@Setter
public class BusinessArea extends AbstractDomainAggregateRoot<BusinessArea> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Long id;
    private Long userId;
    private String name;
    private String floor;
    private String description;
    private String icon;

    // US-32: Shared notification recipients.
    private final Set<String> notificationRecipients =
            new LinkedHashSet<>();

    public BusinessArea(
            Long id,
            Long userId,
            String name,
            String floor,
            String description,
            String icon) {

        this.id = id;
        this.userId = Objects.requireNonNull(
                userId,
                "userId must not be null"
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Business area name must not be blank"
            );
        }

        this.name = name.trim();

        this.floor = (floor != null && !floor.isBlank())
                ? floor.trim()
                : "Piso 1";

        this.description = description != null
                ? description.trim()
                : "";

        this.icon = (icon != null && !icon.isBlank())
                ? icon.trim()
                : "storefront";
    }

    public BusinessArea(
            Long id,
            Long userId,
            String name,
            String description) {

        this(
                id,
                userId,
                name,
                "Piso 1",
                description,
                "storefront"
        );
    }

    public BusinessArea(CreateBusinessAreaCommand command) {

        this(
                null,
                command.userId(),
                command.name(),
                command.floor(),
                command.description(),
                command.icon()
        );
    }

    /**
     * Updates details of the business area (US-31 Escenario 3).
     */
    public void updateDetails(
            String name,
            String floor,
            String description,
            String icon) {

        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }

        if (floor != null && !floor.isBlank()) {
            this.floor = floor.trim();
        }

        if (description != null) {
            this.description = description.trim();
        }

        if (icon != null && !icon.isBlank()) {
            this.icon = icon.trim();
        }
    }

    /**
     * US-32: Adds a shared notification recipient.
     * Duplicate email addresses are automatically prevented.
     */
    public void addNotificationRecipient(String email) {

        String normalizedEmail = normalizeEmail(email);

        notificationRecipients.add(normalizedEmail);
    }

    /**
     * US-32: Removes a shared notification recipient.
     */
    public void removeNotificationRecipient(String email) {

        String normalizedEmail = normalizeEmail(email);

        notificationRecipients.remove(normalizedEmail);
    }

    /**
     * US-32: Replaces the configured notification recipients.
     */
    public void replaceNotificationRecipients(List<String> emails) {

        Objects.requireNonNull(
                emails,
                "Notification recipients must not be null"
        );

        Set<String> validatedRecipients = new LinkedHashSet<>();

        for (String email : emails) {
            validatedRecipients.add(normalizeEmail(email));
        }

        notificationRecipients.clear();
        notificationRecipients.addAll(validatedRecipients);
    }

    /**
     * Returns an immutable snapshot of configured recipients.
     */
    public Set<String> getNotificationRecipients() {
        return Set.copyOf(notificationRecipients);
    }

    /**
     * Indicates whether the area has shared notification recipients.
     */
    public boolean hasNotificationRecipients() {
        return !notificationRecipients.isEmpty();
    }

    /**
     * Validates and normalizes an email address.
     */
    private String normalizeEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification recipient email must not be blank"
            );
        }

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (normalizedEmail.length() > 254
                || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {

            throw new IllegalArgumentException(
                    "Invalid notification recipient email"
            );
        }

        return normalizedEmail;
    }
}
