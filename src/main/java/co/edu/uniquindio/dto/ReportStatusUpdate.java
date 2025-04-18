package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportStatusUpdate(
        @NotBlank String reportId,
        @NotNull ReportStatus newStatus,
        String rejectionReason // solo obligatorio si es REJECTED
) {
}
