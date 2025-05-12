package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.CategoryStatus;

public record CategoryResponse(
        String id,
        String name,
        String description,
        CategoryStatus status
) {
}
