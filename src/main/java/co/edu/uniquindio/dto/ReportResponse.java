package co.edu.uniquindio.dto;

import lombok.Builder;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.List;
@Builder
public record ReportResponse(
        String id,
        String title,
        String description,
        String date,
        GeoJsonPoint location,
        List<String> categories,
        String status,
        int ratingsImportant,
        String userId,
        List<String> imageUrls,
        String rejectionReason
) {

}