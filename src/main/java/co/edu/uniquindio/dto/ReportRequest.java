package co.edu.uniquindio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReportRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotNull
        List<String> categories,
        @NotNull
        GeoJsonPoint location,
        List<MultipartFile> images
){
}
