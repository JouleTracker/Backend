
package com.jouletracker.platform.monitoringspace.domain.model.aggregates;

import com.jouletracker.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.util.Set;

/**
 * US-48: Aggregate root representing the
 * monitoring space configuration of a user's home.
 */
@Getter
public class HomeConfiguration
        extends AbstractDomainAggregateRoot<HomeConfiguration> {

    private static final Set<String> VALID_HOME_TYPES =
            Set.of("House", "Apartment", "Office/Premises");

    private final Long userId;

    private String homeType;
    private Integer occupants;
    private String location;
    private String tariff;

    public HomeConfiguration(Long userId) {
        this(userId, "House", 1, "", "");
    }

    public HomeConfiguration(
            Long userId,
            String homeType,
            Integer occupants,
            String location,
            String tariff) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero"
            );
        }

        this.userId = userId;

        updateConfiguration(
                homeType,
                occupants,
                location,
                tariff
        );
    }

    public void updateConfiguration(
            String homeType,
            Integer occupants,
            String location,
            String tariff) {

        if (homeType == null
                || !VALID_HOME_TYPES.contains(homeType)) {

            throw new IllegalArgumentException(
                    "Invalid home type"
            );
        }

        if (occupants == null || occupants < 1) {

            throw new IllegalArgumentException(
                    "Occupants must be greater than zero"
            );
        }

        String normalizedLocation =
                location == null ? "" : location.trim();

        String normalizedTariff =
                tariff == null ? "" : tariff.trim();

        if (normalizedLocation.length() > 255) {

            throw new IllegalArgumentException(
                    "Location must not exceed 255 characters"
            );
        }

        if (normalizedTariff.length() > 100) {

            throw new IllegalArgumentException(
                    "Tariff must not exceed 100 characters"
            );
        }

        this.homeType = homeType;
        this.occupants = occupants;
        this.location = normalizedLocation;
        this.tariff = normalizedTariff;
    }
}
