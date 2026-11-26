package com.jouletracker.platform.iam.interfaces.rest.resources;

import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST request payload for completing a password reset with a single-use recovery token (TS-03).
 *
 * @param token       the single-use password reset token delivered via email
 * @param newPassword the new password complying with platform complexity rules
 */
@Schema(description = "Datos para restablecer la contraseña de la cuenta usando un token de un solo uso")
public record ResetPasswordRequest(
        @Schema(description = "Token de recuperación de contraseña de un solo uso recibido por correo", example = "9xK2vR4pL8nQ1mZ7wB5cD3fG6hJ0kT2yU4vW8xY1zA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El token de recuperación de contraseña es obligatorio")
        String token,

        @Schema(description = "Nueva contraseña (entre 8 y 128 caracteres, con mayúscula, minúscula, número y carácter especial)", example = "NewSecurePass123#", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = "La nueva contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = PasswordPolicy.PASSWORD_REGEX,
                message = PasswordPolicy.POLICY_MESSAGE
        )
        String newPassword) {
}
