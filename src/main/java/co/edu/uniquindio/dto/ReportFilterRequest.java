package co.edu.uniquindio.dto;

import java.time.LocalDate;
import java.util.List;

public record ReportFilterRequest(
        List<String> categories,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        Double latitude,
        Double longitude,
        Double radiusKm
) {}
