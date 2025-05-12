package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Builder
public record ReportRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotNull
        List<String> categories,
        @NotNull
        LocationDTO location,

        List<MultipartFile> images
){
}
