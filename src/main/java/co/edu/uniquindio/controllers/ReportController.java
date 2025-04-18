package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.*;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.utils.JwtUtil;
import co.edu.uniquindio.services.interfaces.ReportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ReportResponse> createReport(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("location") String locationJson,
            @RequestParam("categories") String categoriesJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam("userId") String userId
    ) {
        ObjectMapper mapper = new ObjectMapper();
        LocationDTO location;
        List<String> categories;
        try {
            location = mapper.readValue(locationJson, LocationDTO.class);
            categories = mapper.readValue(categoriesJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir datos JSON", e);
        }

        ReportRequest request = new ReportRequest(title, description, categories, location, images);
        ReportResponse response = reportService.createReport(request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{reportId}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable String reportId,
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("report") String reportJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // 1. Extrae el ID del usuario desde el token
        String token = authHeader.replace("Bearer ", "").trim();
        String userId = jwtUtil.extractUserId(token);

        // 2. Parsea el JSON del reporte
        ObjectMapper mapper = new ObjectMapper();
        ReportRequest request;
        try {
            request = mapper.readValue(reportJson, ReportRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear el reporte", e);
        }

        // 3. Actualiza el request si hay imágenes nuevas
        if (images != null) {
            request = new ReportRequest(
                    request.title(),
                    request.description(),
                    request.categories(),
                    request.location(),
                    images
            );
        }

        // 4. Procesa la actualización
        ReportResponse updatedReport = reportService.updateReport(request, reportId, userId);
        return ResponseEntity.ok(updatedReport);
    }
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable String reportId,
            @RequestParam @NotNull ReportStatus status,
            @RequestParam(required = false) String rejectionReason,
            @RequestHeader("X-User-Id") String userId) {

        ReportResponse response = reportService.updateReportStatus(reportId, status, rejectionReason, userId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public void filterReportsAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radius,
            @RequestParam(defaultValue = "web") String format,
            HttpServletResponse response,
            @RequestHeader("Authorization") String authHeader  // Añade este parámetro
    ) throws IOException {

        // Extrae el ID del admin del token
        String token = authHeader.replace("Bearer ", "").trim();
        String adminId = jwtUtil.extractUserId(token);

        if ("pdf".equalsIgnoreCase(format)) {
            reportService.generatePdfReport(status, categories, startDate, endDate, lat, lng, radius, response);
        } else {
            List<ReportResponse> reports = reportService.getReportsWithFilters(
                    new ReportFilterRequest(categories, status, startDate, endDate, lat, lng, radius),
                    adminId  // Pasa el ID real en lugar de null
            );
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), reports);
        }
    }


    /*
    @PostMapping("/{reportId}/toggle-importance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleReportImportance(
            @PathVariable String reportId,
            @RequestHeader("Authorization") String authHeader) {

        // Obtener ID del usuario autenticado desde el token JWT
        String token = authHeader.replace("Bearer ", "").trim();
        String userId = jwtUtil.extractUserId(token);

        reportService.toggleReportImportance(reportId, userId);
        return ResponseEntity.ok().build();
    }


    // Obtener todos los reportes de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable String userId) {
        List<ReportResponse> reports = reportService.getReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }

    // Obtener un reporte por su ID
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable String reportId) {
        ReportResponse response = reportService.getReportById(reportId);
        return ResponseEntity.ok(response);
    }

     */



}
