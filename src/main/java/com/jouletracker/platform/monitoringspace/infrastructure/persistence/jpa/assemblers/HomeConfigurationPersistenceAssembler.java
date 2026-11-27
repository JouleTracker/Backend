
package com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.assemblers;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.monitoringspace.infrastructure.persistence.jpa.entities.HomeConfigurationPersistenceEntity;

/**
 * US-48: Converts home configurations between
 * domain and persistence representations.
 */
public final class HomeConfigurationPersistenceAssembler {

    private HomeConfigurationPersistenceAssembler() {
    }

    public static HomeConfiguration toDomainFromPersistence(
            HomeConfigurationPersistenceEntity entity) {

        return new HomeConfiguration(
                entity.getUserId(),
                entity.getHomeType(),
                entity.getOccupants(),
                entity.getLocation(),
                entity.getTariff()
        );
    }

    public static HomeConfigurationPersistenceEntity toPersistenceFromDomain(
            HomeConfiguration configuration) {

        var entity = new HomeConfigurationPersistenceEntity();

        entity.setUserId(configuration.getUserId());
        entity.setHomeType(configuration.getHomeType());
        entity.setOccupants(configuration.getOccupants());
        entity.setLocation(configuration.getLocation());
        entity.setTariff(configuration.getTariff());

        return entity;
    }
}
