package co.edu.uniquindio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActivateUserRequest(
        @Email(message = "Debe ser un email válido")
        @NotBlank(message = "El email es requerido")
        String email,

        @NotBlank(message = "El código de activación es requerido")
        String code
) {
}
