package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDate;

public record UserResponse(
        @NotBlank(message = "El campo es requerido")
        String id,

        @NotBlank(message = "El campo es requerido")
        @Email(message = "Debe ser un email válido")
        String email,

        @NotBlank(message = "El campo es requerido")
        @Size(max = 100, message = "No debe exceder los 100 caracteres")
        String fullName,

        @NotNull(message = "La fecha no puede ser nula")
        @PastOrPresent(message = "La fecha no puede ser futura")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dateBirth,

        @NotBlank(message = "El campo es requerido")
        String phone,

        @NotBlank(message = "El campo es requerido")
        String address,

        @NotBlank(message = "El campo es requerido")
        String city,

        Role role,

        UserStatus status,

        GeoJsonPoint location  // Nuevo campo

) {
}
