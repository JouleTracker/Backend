package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * REST request payload for resending an account verification email (US-04).
 *
 * @param email the unverified account email address
 */
@Schema(description = "Datos para reenviar el enlace de verificación de correo")
public record ResendVerificationRequest(
        @Schema(description = "Correo electrónico de la cuenta no verificada", example = "john.doe@jouletracker.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        String email) {
}
