package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.Rol;


public record UserResponse(
        String id,
        String fullName,
        String email,
        Rol rol
) {}

