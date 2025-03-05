package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserRegistrationRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotNull LocalDate dateBirth,
        @NotNull Rol rol
) {}
