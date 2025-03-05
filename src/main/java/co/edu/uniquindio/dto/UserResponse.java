package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Rol;
import java.time.LocalDate;

public record UserResponse(
        String id,
        String email,
        String fullName,
        LocalDate dateBirth,
        Rol rol
) {}

