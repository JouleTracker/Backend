package com.jouletracker.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user sign-in authentication (POST /api/v1/authentication/sign-in).
 */
@Schema(description = "Credenciales de autenticación del usuario")
public record SignInRequest(
        @Schema(description = "Correo electrónico registrado", example = "john.doe@jouletracker.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        String email,

        @Schema(description = "Contraseña del usuario", example = "SecurePass123!")
        @NotBlank(message = "La contraseña es obligatoria")
        String password) {
}
