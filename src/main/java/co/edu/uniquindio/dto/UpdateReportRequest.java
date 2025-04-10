package co.edu.uniquindio.dto;

import java.util.List;

public record UpdateReportRequest(
        String title,
        String description,
        List<String> categories,
        List<String> imageUrls,
        double latitude,
        double longitude

) {
}
