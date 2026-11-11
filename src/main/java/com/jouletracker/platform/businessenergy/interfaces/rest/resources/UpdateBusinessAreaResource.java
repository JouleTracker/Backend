package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request body to update an existing business area (US-31 Escenario 3).
 */
@Schema(description = "Request body to partially update a business area")
public record UpdateBusinessAreaResource(

        @Size(max = 100)
        @Schema(
                description = "Updated name of the physical business area",
                example = "Almacén Central y Despacho"
        )
        String name,

        @Schema(
                description = "Updated floor or level",
                example = "Piso 2"
        )
        String floor,

        @Size(max = 255)
        @Schema(
                description = "Updated description",
                example = "Zona ampliada para almacenamiento y distribución"
        )
        String description,

        @Schema(
                description = "Updated Material icon identifier",
                example = "inventory_2"
        )
        String icon
) {
}
