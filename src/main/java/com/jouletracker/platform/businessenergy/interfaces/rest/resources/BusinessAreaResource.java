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
                example = "Almacén"
        )
        String name,

        @Schema(
                description = "Floor or level of the physical zone",
                example = "Piso 1"
        )
        String floor,

        @Schema(
                description = "Description of the business area",
                example = "Área destinada al almacenamiento de insumos"
        )
        String description,

        @Schema(
                description = "Material icon identifier for UI rendering",
                example = "storefront"
        )
        String icon
) {
}
