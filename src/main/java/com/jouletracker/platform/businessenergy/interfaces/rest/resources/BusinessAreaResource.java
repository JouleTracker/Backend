package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST response representation of a business area.
 */
@Schema(description = "Response representation of a physical business area")
public record BusinessAreaResource(

        @Schema(
                description = "Unique business area identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Owner user identifier",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "Name of the physical business area",
                example = "Almacen"
        )
        String name,

        @Schema(
                description = "Description of the business area",
                example = "Area destinada al almacenamiento de productos"
        )
        String description
) {
}
