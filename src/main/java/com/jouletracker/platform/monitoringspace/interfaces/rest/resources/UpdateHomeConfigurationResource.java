
package com.jouletracker.platform.monitoringspace.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * US-48: Request resource for updating
 * a user's home configuration.
 */
public record UpdateHomeConfigurationResource(

        @NotBlank(message = "Home type is required")
        @Pattern(
                regexp = "House|Apartment|Office/Premises",
                message = "Invalid home type"
        )
        @Schema(example = "House")
        String homeType,

        @NotNull(message = "Number of occupants is required")
        @Min(
                value = 1,
                message = "Occupants must be greater than zero"
        )
        @Schema(example = "4")
        Integer occupants,

        @Size(
                max = 255,
                message = "Location must not exceed 255 characters"
        )
        @Schema(example = "Chorrillos, Lima")
        String location,

        @Size(
                max = 100,
                message = "Tariff must not exceed 100 characters"
        )
        @Schema(example = "BT5B")
        String tariff

) {
}
