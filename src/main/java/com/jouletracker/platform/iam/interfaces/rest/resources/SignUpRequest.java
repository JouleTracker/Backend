package com.jouletracker.platform.iam.interfaces.rest.resources;

import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Request payload for user registration (POST /api/v1/authentication/sign-up).
 */
@Schema(description = "Datos de registro de usuario")
public record SignUpRequest(
        @Schema(description = "Correo electrónico del usuario", example = "john.doe@jouletracker.com")
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        String email,

        @Schema(description = "Contraseña en texto plano (entre 8 y 128 caracteres, con mayúscula, minúscula, número y carácter especial)", example = "SecurePass123#")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = PasswordPolicy.PASSWORD_REGEX,
                message = PasswordPolicy.POLICY_MESSAGE
        )
        String password,

        @Schema(description = "Nombre del usuario", example = "John")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
        String firstName,

        @Schema(description = "Apellido del usuario (opcional)", example = "Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 100, message = "El apellido no debe exceder los 100 caracteres")
        @Nullable String lastName) {
}
