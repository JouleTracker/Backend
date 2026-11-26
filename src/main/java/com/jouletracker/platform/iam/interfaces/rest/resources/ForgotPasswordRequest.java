package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request payload for initiating password recovery (TS-03).
 *
 * @param email the registered account email address
 */
@Schema(description = "Datos para iniciar la recuperación de contraseña")
public record ForgotPasswordRequest(
        @Schema(description = "Correo electrónico registrado del usuario", example = "john.doe@jouletracker.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        String email) {
}
