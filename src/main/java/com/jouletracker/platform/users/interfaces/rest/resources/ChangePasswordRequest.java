package com.jouletracker.platform.users.interfaces.rest.resources;

import com.jouletracker.platform.shared.domain.model.valueobjects.PasswordPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * REST request payload for changing an authenticated user's password (US-25).
 *
 * @param currentPassword the user's current password for BCrypt verification
 * @param newPassword     the desired new password complying with platform complexity rules
 */
@Schema(description = "Datos para actualizar la contraseña del usuario autenticado")
public record ChangePasswordRequest(
        @Schema(description = "Contraseña actual de la cuenta", example = "CurrentPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña actual es obligatoria")
        String currentPassword,

        @Schema(description = "Nueva contraseña (entre 8 y 128 caracteres, con mayúscula, minúscula, número y carácter especial)", example = "NewSecurePass456#", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = "La nueva contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = PasswordPolicy.PASSWORD_REGEX,
                message = PasswordPolicy.POLICY_MESSAGE
        )
        String newPassword) {
}
