
package com.jouletracker.platform.businessenergy.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * REST response representation of a business area.
 *
 * US-32: Includes shared notification recipients
 * associated with the business area.
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
        String icon,

        @Schema(
                description = "Shared notification recipient email addresses",
                example = "[\"admin@example.com\", \"supervisor@example.com\"]"
        )
        List<String> notificationRecipients

) {

        public BusinessAreaResource {

                notificationRecipients = notificationRecipients != null
                        ? List.copyOf(notificationRecipients)
                        : List.of();
        }

        /**
         * Maintains compatibility with existing response constructors.
         */
        public BusinessAreaResource(
                Long id,
                Long userId,
                String name,
                String floor,
                String description,
                String icon) {

                this(
                        id,
                        userId,
                        name,
                        floor,
                        description,
                        icon,
                        List.of()
                );
        }
}
