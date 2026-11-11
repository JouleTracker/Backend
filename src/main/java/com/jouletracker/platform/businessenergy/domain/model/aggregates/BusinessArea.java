package com.jouletracker.platform.businessenergy.domain.model.aggregates;

import com.jouletracker.platform.businessenergy.domain.model.commands.CreateBusinessAreaCommand;
import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Aggregate Root representing a physical area of an MYPE business.
 * Each area belongs to one user and can be used to organize energy monitoring.
 */
@Getter
@Setter
public class BusinessArea extends AbstractDomainAggregateRoot<BusinessArea> {

    private Long id;
    private Long userId;
    private String name;
    private String floor;
    private String description;
    private String icon;

    public BusinessArea(Long id, Long userId, String name, String floor, String description, String icon) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId must not be null");

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Business area name must not be blank");
        }

        this.name = name.trim();
        this.floor = (floor != null && !floor.isBlank()) ? floor.trim() : "Piso 1";
        this.description = description != null ? description.trim() : "";
        this.icon = (icon != null && !icon.isBlank()) ? icon.trim() : "storefront";
    }

    public BusinessArea(Long id, Long userId, String name, String description) {
        this(id, userId, name, "Piso 1", description, "storefront");
    }

    public BusinessArea(CreateBusinessAreaCommand command) {
        this(null, command.userId(), command.name(), command.floor(), command.description(), command.icon());
    }

    /**
     * Updates details of the business area (US-31 Escenario 3).
     */
    public void updateDetails(String name, String floor, String description, String icon) {
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
}
