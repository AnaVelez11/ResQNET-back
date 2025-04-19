package co.edu.uniquindio.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectReportRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String reason
) {
}
