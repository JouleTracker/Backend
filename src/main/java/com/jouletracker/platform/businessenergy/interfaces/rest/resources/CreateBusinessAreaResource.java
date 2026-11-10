package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body to register a new physical business area.
 */
@Schema(description = "Request body to create a business area")
public record CreateBusinessAreaResource(

        @NotBlank(message = "Business area name must not be blank")
        @Size(max = 100)
        @Schema(
                description = "Name of the physical business area",
                example = "Almacen"
        )
        String name,

        @Size(max = 255)
        @Schema(
                description = "Optional description of the business area",
                example = "Area destinada al almacenamiento de productos"
        )
        String description,

        @Schema(
                description = "Owner user identifier",
                example = "1"
        )
        Long userId
) {
}
