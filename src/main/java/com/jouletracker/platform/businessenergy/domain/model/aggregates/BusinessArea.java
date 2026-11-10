package com.jouletracker.platform.businessenergy.domain.model.aggregates;

import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Objects;

/**
 * Aggregate Root representing a physical area of an MYPE business.
 * Each area belongs to one user and can be used to organize energy monitoring.
 */
@Getter
public class BusinessArea extends AbstractDomainAggregateRoot<BusinessArea> {

    private final Long id;
    private final Long userId;
    private final String name;
    private final String description;

    public BusinessArea(Long id, Long userId, String name, String description) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Business area name must not be blank");
        }

        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
    }

    public BusinessArea(CreateBusinessAreaCommand command) {
        this(null, command.userId(), command.name(), command.description());
    }
}
