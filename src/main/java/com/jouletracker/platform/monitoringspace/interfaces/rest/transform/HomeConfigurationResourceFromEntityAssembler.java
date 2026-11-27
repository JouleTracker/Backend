
package com.jouletracker.platform.monitoringspace.interfaces.rest.transform;

import com.jouletracker.platform.monitoringspace.domain.model.aggregates.HomeConfiguration;
import com.jouletracker.platform.monitoringspace.interfaces.rest.resources.HomeConfigurationResource;

/**
 * US-48: Converts home configuration aggregates
 * into REST response resources.
 */
public final class HomeConfigurationResourceFromEntityAssembler {

    private HomeConfigurationResourceFromEntityAssembler() {
    }

    public static HomeConfigurationResource toResourceFromEntity(
            HomeConfiguration configuration) {

        return new HomeConfigurationResource(
                configuration.getUserId(),
                configuration.getHomeType(),
                configuration.getOccupants(),
                configuration.getLocation(),
                configuration.getTariff(),
                true
        );
    }

    public static HomeConfigurationResource toDefaultResource(
            Long userId) {

        var configuration = new HomeConfiguration(userId);

        return new HomeConfigurationResource(
                configuration.getUserId(),
                configuration.getHomeType(),
                configuration.getOccupants(),
                configuration.getLocation(),
                configuration.getTariff(),
                false
        );
    }
}
