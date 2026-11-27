
package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body to update an existing business area
 * (US-31 Escenario 3).
 *
 * US-32: Supports updating shared notification recipients.
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
        String icon,

        @Schema(
                description = "Updated shared notification recipients. "
                        + "Null preserves existing recipients. "
                        + "An empty list removes all recipients.",
                example = "[\"admin@example.com\", \"supervisor@example.com\"]"
        )
        List<
                @NotBlank(message = "Recipient email must not be blank")
                @Email(message = "Invalid notification recipient email")
                @Size(max = 254)
                        String
                > notificationRecipients

) {

        /**
         * Maintains compatibility with existing update resources.
         */
        public UpdateBusinessAreaResource(
                String name,
                String floor,
                String description,
                String icon) {

                this(
                        name,
                        floor,
                        description,
                        icon,
                        null
                );
        }
}
