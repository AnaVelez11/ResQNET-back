package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    Location location,
    List<MultipartFile> images
){
}
