package com.jouletracker.platform.users.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * REST request payload for administrative user moderation status updates (US-49).
 *
 * @param status target account status (ACTIVE, SUSPENDED, BANNED, INACTIVE)
 * @param reason optional administrative moderation reason
 */
@Schema(description = "Datos para actualizar el estado de moderación de una cuenta de usuario")
public record UpdateUserStatusRequest(
        @Schema(
                description = "Estado objetivo de la cuenta",
                example = "BANNED",
                allowableValues = {"ACTIVE", "SUSPENDED", "BANNED", "INACTIVE"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "El estado de la cuenta es obligatorio")
        String status,

        @Schema(description = "Motivo opcional de la acción de moderación", example = "Incumplimiento de los términos del servicio")
        @Size(max = 255, message = "El motivo de moderación no debe exceder los 255 caracteres")
        @Nullable String reason) {
}
