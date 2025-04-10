package co.edu.uniquindio.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateReportRequest(

        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotBlank(message = "La descripción es obligatoria")
        String description,

        @NotNull(message = "La latitud es obligatoria")
        Double latitude,

        @NotNull(message = "La longitud es obligatoria")
        Double longitude,

        @NotEmpty(message = "Debe tener al menos una categoría")
        List<String> categories,

        @Size(min = 1, message = "Debe incluir al menos una imagen")
        List<String> imageUrls

) {}
