package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.Location;
import lombok.Builder;

import java.util.List;
@Builder
public record ReportResponse(
        String id,
        String title,
        String description,
        String date,
        Location location,
        List<String> categories,
        String status,
        int ratingsImportant,
        String userId,
        List<String> imageUrls,
        String rejectionReason
) {

}
