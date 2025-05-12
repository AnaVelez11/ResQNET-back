package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;


import java.time.LocalDate;
import java.util.Objects;


public record UserRegistrationRequest(
        @NotBlank(message = "El campo es requerido")
        String id,

        @NotBlank(message = "El campo es requerido")
        @Email(message = "Debe ser un email válido")
        String email,

        @NotBlank(message = "El campo es requerido")
        @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$", message = "Debe contener al menos: un número, una letra minúscula y una mayúscula")
        @Size(min = 8, message = "La longitud mínima es 8 caracteres")
        String password,

        @NotBlank(message = "El campo es requerido")
        @Size(max = 100, message = "No debe exceder los 100 caracteres")
        String fullName,

        @NotNull(message = "La fecha no puede ser nula")
        @PastOrPresent(message = "La fecha no puede ser futura")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @NotBlank(message = "El campo es requerido")
        String phone,

        @NotBlank(message = "El campo es requerido")
        String address,

        @NotBlank(message = "El campo es requerido")
        String city,

        GeoJsonPoint location,
        Role role) {
        public UserRegistrationRequest{
                role = Objects.requireNonNullElse(role, Role.CLIENT);
        }
}
