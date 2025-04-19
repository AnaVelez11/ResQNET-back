package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.*;
import co.edu.uniquindio.exceptions.ForbiddenActionException;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.utils.JwtUtil;
import co.edu.uniquindio.services.interfaces.ReportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    /// / Crear un nuevo reporte con datos multipart (texto + imágenes)
    ///
    /// / Retorna reporte creado con todos sus datos
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
            categories = mapper.readValue(categoriesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir datos JSON", e);
        }

        ReportRequest request = new ReportRequest(title, description, categories, location, images);
        ReportResponse response = reportService.createReport(request, userId);
        return ResponseEntity.ok(response);
    }

    /// / Actualizar un reporte existente (con posibles nuevas imágenes)
    ///
    /// / Retorna reporte actualizado
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

    /// / Cambiar el estado de un reporte (Admin/Usuario)
    ///
    /// / Retorna reporte con estado actualizado
    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable String reportId,
            @RequestParam @NotNull ReportStatus status,
            @RequestParam(required = false) String rejectionReason,
            @RequestHeader("X-User-Id") String userId) {

        ReportResponse response = reportService.updateReportStatus(reportId, status, rejectionReason, userId);
        return ResponseEntity.ok(response);
    }

    /// / Filtrar reportes (Admin) con salida en PDF o JSON
    ///
    /// / Retorna lista de reportes filtrados o PDF generado
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
            @RequestHeader("Authorization") String authHeader
    ) throws IOException {

        // Extrae el ID del admin del token
        String token = authHeader.replace("Bearer ", "").trim();
        String adminId = jwtUtil.extractUserId(token);

        if ("pdf".equalsIgnoreCase(format)) {
            reportService.generatePdfReport(status, categories, startDate, endDate, lat, lng, radius, response);
        } else {
            List<ReportResponse> reports = reportService.getReportsWithFilters(
                    new ReportFilterRequest(categories, status, startDate, endDate, lat, lng, radius),
                    adminId
            );
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), reports);
        }
    }

    /// / Eliminar un reporte (Usuario/Admin)
    ///
    /// / Retorna confirmación de eliminación
    @DeleteMapping("/{reportId}")
    public ResponseEntity<String> deleteReport(
            @PathVariable String reportId,
            @RequestHeader("Authorization") String token
    ) {
        String userId = jwtUtil.getUserIdFromToken(token);

        reportService.deleteReport(reportId, userId);

        return ResponseEntity.ok("Reporte eliminado exitosamente");
    }

    /// / Marcar/desmarcar reporte como importante
    ///
    /// / Retorna status 200 sin contenido
    @PostMapping("/{reportId}/toggle-importance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleReportImportance(
            @PathVariable String reportId,
            @RequestHeader("Authorization") String authHeader) {

        // Extraer el token del header
        String token = authHeader.replace("Bearer ", "").trim();

        // Obtener ID del usuario desde el token
        String userId = jwtUtil.extractUserId(token);

        // Llamar al servicio
        reportService.toggleReportImportance(reportId, userId);

        return ResponseEntity.ok().build();
    }

    /// / Obtener usuarios que marcaron un reporte como importante (Admin)
    ///
    /// / Retorna lista de IDs de usuarios

    @GetMapping("/reports/{reportId}/liked-by")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getLikedBy(@PathVariable String reportId) {
        return ResponseEntity.ok(reportService.getLikedBy(reportId));
    }

    /// / Obtener reportes marcados como importantes por un usuario
    ///
    /// / Retorna lista de IDs de reportes
    @GetMapping("/users/{userId}/liked-reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getLikedReports(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getLikedReports(userId));
    }

    /// / Rechazar un reporte con motivo (Admin)
    ///
    /// / Retorna reporte rechazado con motivo y fecha límite
    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ReportResponse> rejectReportWithReason(
            @PathVariable String reportId,
            @Valid @RequestBody RejectReportRequest request,
            @RequestHeader("X-User-Id") String adminId)
            throws ResourceNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(
                reportService.rejectReportWithReason(
                        reportId,
                        request.reason(),
                        adminId
                )
        );
    }

    /// / Reenviar un reporte rechazado después de correcciones
    ///
    /// / Retorna reporte en estado PENDING para revisión
    @PutMapping("/{reportId}/resubmit")
    public ResponseEntity<ReportResponse> resubmitReport(
            @PathVariable String reportId,
            @Valid @RequestBody ReportRequest request,
            @RequestHeader("X-User-Id") String userId)
            throws ResourceNotFoundException, ForbiddenActionException {

        return ResponseEntity.ok(
                reportService.resubmitReport(request, reportId, userId)
        );
    }

    /// // Obtener todos los reportes de un usuario específico
    ///
    /// / Retorna lista de reportes del usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable String userId) {
        List<ReportResponse> reports = reportService.getReportsByUserId(userId);
        return ResponseEntity.ok(reports);
    }

    /// / Obtener un reporte específico por su ID
    ///
    /// / Retorna detalle completo del reporte
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable String reportId) {
        ReportResponse response = reportService.getReportById(reportId);
        return ResponseEntity.ok(response);
    }


}
