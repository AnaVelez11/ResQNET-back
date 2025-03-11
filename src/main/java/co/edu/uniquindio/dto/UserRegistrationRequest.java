package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Rol;
import co.edu.uniquindio.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRegistrationRequest(
        String id,
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String cellphoneNumber,
        @NotNull Rol rol,
        @NotNull UserStatus userStatus
) {}

