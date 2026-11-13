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
                example = "Almacén Principal"
        )
        String name,

        @Schema(
                description = "Floor or level of the physical zone",
                example = "Piso 1"
        )
        String floor,

        @Size(max = 255)
        @Schema(
                description = "Optional description of the business area",
                example = "Área destinada al almacenamiento de insumos y mercadería"
        )
        String description,

        @Schema(
                description = "Material icon identifier for UI rendering",
                example = "storefront"
        )
        String icon,

        @Schema(
                description = "Owner user identifier",
                example = "1"
        )
        Long userId
) {
}
