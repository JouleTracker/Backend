
package com.jouletracker.platform.monitoringspace.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * US-48: Response resource representing
 * a user's home configuration.
 */
public record HomeConfigurationResource(

        @Schema(example = "1")
        Long userId,

        @Schema(example = "House")
        String homeType,

        @Schema(example = "4")
        Integer occupants,

        @Schema(example = "Chorrillos, Lima")
        String location,

        @Schema(example = "BT5B")
        String tariff,

        @Schema(
                description = "Indicates whether the configuration has been persisted",
                example = "true"
        )
        boolean configured

) {
}

