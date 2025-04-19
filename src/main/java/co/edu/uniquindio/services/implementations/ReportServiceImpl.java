package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.dto.ReportFilterRequest;
import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.events.NewReportEvent;
import co.edu.uniquindio.exceptions.BusinessException;
import co.edu.uniquindio.exceptions.ForbiddenActionException;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.mappers.ReportMapper;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.CloudinaryService;
import co.edu.uniquindio.services.interfaces.ReportService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.itextpdf.layout.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReportMapper reportMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailServiceImpl emailService;

    @Override
    public ReportResponse createReport(ReportRequest request, String userId) {
        // 1. Validar usuario
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Validar categorías
        List<String> invalidCategories = new ArrayList<>();
        if (request.categories() != null) {
            request.categories().forEach(categoryId -> {
                boolean exists = categoryRepository.existsById(categoryId); // Verifica existencia sin cargar el objeto
                if (!exists) {
                    invalidCategories.add(categoryId); // Agrega categorías inexistentes a la lista
                }
            });

            // Si hay categorías inválidas, lanza una excepción con los detalles
            if (!invalidCategories.isEmpty()) {
                throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", invalidCategories));
            }
        }

        // 3. Subir imágenes a Cloudinary (si existen)
        List<String> imageUrls = null;
        if (request.images() != null && !request.images().isEmpty()) {
            imageUrls = cloudinaryService.uploadImages(request.images());
        }

        // 4. Convertir a GeoJsonPoint
        GeoJsonPoint geoPoint = new GeoJsonPoint(request.location().longitude(), request.location().latitude());

        // 5. Crear y guardar el reporte
        Report report = Report.builder().title(request.title()).description(request.description()).location(geoPoint).categories(request.categories()).idUser(userId).status(ReportStatus.PENDING).date(LocalDateTime.now()).ratingsImportant(0).imageUrls(imageUrls).build();


        Report savedReport = reportRepository.save(report);

        // 6. Asociar el reporte al usuario
        if (user.getReports() == null) {
            user.setReports(new ArrayList<>());
        }
        user.getReports().add(savedReport.getId());
        userRepository.save(user); // guardar el usuario actualizado

        // Notificar a usuarios cercanos
        notifyNearbyUsers(savedReport, userId);

        // 7. Convertir a DTO y retornar
        return convertToResponse(savedReport, "Reporte creado exitosamente.");
    }

    @Override
    public ReportResponse updateReport(ReportRequest request, String reportId, String userId) {
        // 1. Validar el reporte
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        // Validar que no sea un reporte rechazado con plazo expirado
        if (report.getStatus() == ReportStatus.REJECTED && LocalDateTime.now().isAfter(report.getResubmissionDeadline())) {
            throw new ForbiddenActionException("El plazo para editar este reporte ha expirado");
        }

        // 2. Verificar propiedad
        if (!report.getIdUser().equals(userId)) {
            throw new ForbiddenActionException("No tienes permisos para editar este reporte");
        }

        // Validar que el reporte esté en estado pendiente
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ForbiddenActionException("Solo se pueden editar reportes en estado pendiente");
        }

        // 3. Validar categorías
        if (request.categories() != null) {
            List<String> invalidCategories = request.categories().stream().filter(categoryId -> !categoryRepository.existsById(categoryId)).toList();

            if (!invalidCategories.isEmpty()) {
                throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", invalidCategories));
            }
        }

        // 4. Procesar imágenes
        List<String> imageUrls = request.images() != null && !request.images().isEmpty() ? cloudinaryService.uploadImages(request.images()) : report.getImageUrls();

        // 5. Actualizar el reporte
        report.setTitle(request.title());
        report.setDescription(request.description());
        report.setLocation(new GeoJsonPoint(request.location().longitude(), request.location().latitude()));
        report.setCategories(request.categories());
        report.setImageUrls(imageUrls);
        report.setDate(LocalDateTime.now());

        // 6. Guardar y retornar
        return reportMapper.toResponse(reportRepository.save(report));

    }

    @Override
    public void deleteReport(String reportId, String userId) {
        // 1. Validar existencia del reporte
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Validar que el usuario sea el propietario
        if (!user.getRole().equals(Role.ADMIN) && !report.getIdUser().equals(userId)) {
            throw new ForbiddenActionException("No tienes permisos para eliminar este reporte");
        }

        // 3. Validar que el reporte esté en estado pendiente
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new ForbiddenActionException("Solo se pueden eliminar reportes en estado pendiente");
        }

        // 4. Eliminar el reporte
        reportRepository.deleteById(reportId);
        user.getReports().remove(reportId);
        userRepository.save(user);
        log.info("El usuario {} eliminó el reporte {}", userId, reportId);

    }

    @Override
    public void toggleReportImportance(String reportId, String userId) {
        // 1. Obtener reporte y usuario
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean isLiked = user.getLikedReports().contains(reportId);

        if (isLiked) {
            // Quitar "me gusta"
            user.getLikedReports().remove(reportId);
            report.getLikedBy().remove(userId);
            if (report.getRatingsImportant() > 0) {
                report.setRatingsImportant(report.getRatingsImportant() - 1);
            }
            log.info("Usuario {} quitó 'importancia' del reporte {}", userId, reportId);
        } else {
            // Agregar "me gusta"
            user.getLikedReports().add(reportId);
            report.getLikedBy().add(userId);
            report.setRatingsImportant(report.getRatingsImportant() + 1);
            log.info("Usuario {} marcó como importante el reporte {}", userId, reportId);
        }

        userRepository.save(user);
        reportRepository.save(report);
    }


    @Override
    public ReportResponse updateReportStatus(String reportId, ReportStatus status, String rejectionReason, String userId) {
        log.info("Actualizando estado del reporte {} a {} por usuario {}", reportId, status, userId);

        // 1. Validar existencia del reporte y usuario
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Validar permisos según rol y transición de estado
        validateStatusPermissions(report, status, user);

        // 3. Validar requisitos específicos por estado
        validateStateRequirements(report, status, rejectionReason, user);

        // Registrar cambios
        String oldStatus = report.getStatus().name();
        report.setStatus(status);
        setStatusSpecificData(report, status, rejectionReason, user);

        Report updatedReport = reportRepository.save(report);

        // Notificar cambio de estado
        notifyStatusChange(reportId, oldStatus, status.name(), user.getRole());

        return convertToResponse(updatedReport, "Estado del reporte actualizado exitosamente");
    }

    @Override
    public ReportResponse rejectReportWithReason(String reportId, String rejectionReason, String adminId) {
        // Validar que el usuario es admin
        User admin = userRepository.findById(adminId).orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado"));

        if (!admin.getRole().equals(Role.ADMIN)) {
            throw new ForbiddenActionException("Solo los administradores pueden rechazar reportes");
        }

        // Obtener el reporte
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        // Validar que el reporte está en estado PENDING o VERIFIED
        if (report.getStatus() != ReportStatus.PENDING && report.getStatus() != ReportStatus.VERIFIED) {
            throw new ForbiddenActionException("Solo se pueden rechazar reportes en estado PENDIENTE o VERIFICADO");
        }

        // Validar que se proporcione un motivo
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo de rechazo");
        }

        // Actualizar el reporte
        report.setStatus(ReportStatus.REJECTED);
        report.setRejectionReason(rejectionReason);
        report.setRejectionDate(LocalDateTime.now());
        report.setResubmissionDeadline(LocalDateTime.now().plusDays(5));

        Report updatedReport = reportRepository.save(report);

        // Obtener al usuario del reporte
        User user = userRepository.findById(report.getIdUser()).orElseThrow(() -> new ResourceNotFoundException("Usuario del reporte no encontrado"));

        // Enviar correo de rechazo
        String subject = "Tu reporte ha sido rechazado";
        String body = String.format("""
                Hola %s,
                
                Lamentamos informarte que tu reporte con ID %s ha sido rechazado por la siguiente razón:
                
                "%s"
                
                Tienes un plazo de 5 días para corregir los errores y reenviar el reporte desde la plataforma. 
                Una vez lo envíes, será nuevamente revisado por el equipo de administración.
                
                Si tienes dudas, puedes comunicarte con nuestro equipo de soporte.
                
                Saludos,
                Equipo de soporte ResQNET
                """, user.getName(), report.getId(), rejectionReason);

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            log.error("No se pudo enviar el correo de rechazo al usuario {}: {}", user.getEmail(), e.getMessage());
        }
        // Notificar al usuario (en la app)
        notifyUserAboutRejection(report.getIdUser(), reportId, rejectionReason);

        return convertToResponse(updatedReport, "Reporte rechazado. El usuario tiene 5 días para corregir y reenviar.");
    }

    @Override
    public ReportResponse resubmitReport(ReportRequest request, String reportId, String userId) {
        // Obtener el reporte original
        Report originalReport = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        // Validar que el usuario es el dueño
        if (!originalReport.getIdUser().equals(userId)) {
            throw new ForbiddenActionException("No tienes permisos para reenviar este reporte");
        }

        // Validar que el reporte fue rechazado
        if (originalReport.getStatus() != ReportStatus.REJECTED) {
            throw new ForbiddenActionException("Solo se pueden reenviar reportes rechazados");
        }

        // Validar que no ha expirado el plazo
        if (LocalDateTime.now().isAfter(originalReport.getResubmissionDeadline())) {
            throw new ForbiddenActionException("El plazo de 5 días para reenviar ha expirado");
        }

        // Validar número máximo de reenvíos
        if (originalReport.getResubmissionCount() >= 3) {
            throw new ForbiddenActionException("Has alcanzado el número máximo de reenvíos (3) para este reporte");
        }

        // Validar categorías
        if (request.categories() != null) {
            List<String> invalidCategories = request.categories().stream().filter(categoryId -> !categoryRepository.existsById(categoryId)).toList();

            if (!invalidCategories.isEmpty()) {
                throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", invalidCategories));
            }
        }

        // Subir imágenes si existen
        List<String> imageUrls = request.images() != null && !request.images().isEmpty() ? cloudinaryService.uploadImages(request.images()) : originalReport.getImageUrls();

        // Actualizar el reporte
        originalReport.setTitle(request.title());
        originalReport.setDescription(request.description());
        originalReport.setLocation(new GeoJsonPoint(request.location().longitude(), request.location().latitude()));
        originalReport.setCategories(request.categories());
        originalReport.setImageUrls(imageUrls);
        originalReport.setStatus(ReportStatus.PENDING); // Volver a estado pendiente
        originalReport.setResubmissionCount(originalReport.getResubmissionCount() + 1);
        originalReport.setDate(LocalDateTime.now());

        Report savedReport = reportRepository.save(originalReport);

        log.info("El usuario con ID {} ha reenviado el reporte con ID {}. Reenvíos totales: {}", userId, reportId, originalReport.getResubmissionCount());


        return convertToResponse(savedReport, "Reporte reenviado correctamente. Esperando revisión.");

    }

    @Override
    public List<ReportResponse> getReportsWithFilters(ReportFilterRequest filter, String adminId) {
        validateAdmin(adminId);

        List<Report> filteredReports = reportRepository.findAll().stream().filter(report -> filter.categories() == null || !filter.categories().isEmpty() && report.getCategories().stream().anyMatch(filter.categories()::contains)).filter(report -> filter.status() == null || report.getStatus().name().equalsIgnoreCase(filter.status())).filter(report -> {
            if (filter.startDate() == null && filter.endDate() == null) return true;
            LocalDate date = report.getDate().toLocalDate();
            return (filter.startDate() == null || !date.isBefore(filter.startDate())) && (filter.endDate() == null || !date.isAfter(filter.endDate()));
        }).filter(report -> {
            if (filter.latitude() == null || filter.longitude() == null || filter.radiusKm() == null) return true;
            double distance = calculateDistance(report.getLocation().getY(), report.getLocation().getX(), filter.latitude(), filter.longitude());
            return distance <= filter.radiusKm();
        }).toList();

        return filteredReports.stream().map(report -> convertToResponse(report, "Filtrado exitoso")).toList();

    }

    @Override
    public List<ReportResponse> getReportsByUserId(String userId) {
        // 1. Verifica que el usuario exista
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Obtiene los reportes por sus IDs (asumiendo que User tiene una lista de IDs)
        List<Report> reports = reportRepository.findAllById(user.getReports());

        // 3. Convierte los reportes a respuestas
        return reports.stream().map(report -> convertToResponse(report, "Reporte del usuario")).toList();
    }

    @Override
    public ReportResponse getReportById(String reportId) {
        // Buscar el reporte por su ID
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        // Convertir el reporte a su DTO correspondiente y retornar
        return convertToResponse(report, "Reporte actualizado correctamente.");
    }

    // --- Métodos auxiliares ---
    private ReportResponse convertToResponse(Report report, String message) {
        return ReportResponse.builder().id(report.getId()).title(report.getTitle()).description(report.getDescription()).location(report.getLocation()).categories(report.getCategories()).status(report.getStatus() != null ? report.getStatus().name() : null).ratingsImportant(report.getRatingsImportant()).userId(report.getIdUser()).imageUrls(report.getImageUrls() != null ? report.getImageUrls() : List.of()).date(report.getDate() != null ? report.getDate().toString() : null).rejectionReason(report.getRejectionReason()).message(message).build();

    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void notifyNearbyUsers(Report report, String authorId) {
        final double notificationRadiusKm = 10.0; // Radio configurable
        final double maxDistanceInMeters = notificationRadiusKm * 1000;

        try {
            // 1. Loggeo para diagnóstico
            log.info("Buscando usuarios cerca de reporte ID: {} en ubicación: {},{}", report.getId(), report.getLocation().getX(), report.getLocation().getY());

            // 2. Buscar usuarios cercanos (con manejo de null)
            List<User> nearbyUsers = userRepository.findUsersNearLocation(report.getLocation().getX(), report.getLocation().getY(), maxDistanceInMeters, authorId);

            if (nearbyUsers == null || nearbyUsers.isEmpty()) {
                log.info("No se encontraron usuarios cercanos para notificar");
                return;
            }

            // 3. Procesar notificaciones
            nearbyUsers.stream().filter(user -> user.getLocation() != null).forEach(user -> {
                try {
                    double distance = calculateDistance(report.getLocation().getY(), report.getLocation().getX(), user.getLocation().getY(), user.getLocation().getX());

                    log.debug("Notificando al usuario {} ({} km)", user.getId(), distance);

                    eventPublisher.publishEvent(new NewReportEvent(this, report, user.getId(), distance));
                } catch (Exception e) {
                    log.error("Error notificando al usuario " + user.getId(), e);
                }
            });

        } catch (Exception e) {
            log.error("Error en búsqueda de usuarios cercanos", e);
            // Puedes agregar métricas o alertas aquí
        }
    }

    private void validateAdmin(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenActionException("Solo los administradores pueden generar informes");
        }
    }

    @Override
    public void generatePdfReport(String status, List<String> categories, LocalDate startDate, LocalDate endDate, Double lat, Double lng, Double radius, HttpServletResponse response) throws IOException {

        List<Report> filteredReports = filterReports(status, categories, startDate, endDate, lat, lng, radius);

        // Configurar el PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reportes.pdf");

        // Generar el PDF
        try (PdfWriter writer = new PdfWriter(response.getOutputStream()); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf)) {

            document.add(new Paragraph("Reportes filtrados").setBold().setFontSize(20));
            document.add(new Paragraph("Total: " + filteredReports.size()));

            for (Report report : filteredReports) {
                document.add(new Paragraph("\nTítulo: " + report.getTitle()).setBold());
                document.add(new Paragraph("Descripción: " + report.getDescription()));
                document.add(new Paragraph("Estado: " + report.getStatus()));
                document.add(new Paragraph("Fecha: " + report.getDate()));
            }
        }
    }

    private List<Report> filterReports(String status, List<String> categories, LocalDate startDate, LocalDate endDate, Double lat, Double lng, Double radius) {

        // Obtener todos los reportes (o usar una consulta filtrada si tienes MongoDB/Spring Data)
        List<Report> allReports = reportRepository.findAll();

        // Filtrar manualmente con Streams
        return allReports.stream().filter(report -> status == null || report.getStatus().name().equalsIgnoreCase(status)).filter(report -> categories == null || report.getCategories().stream().anyMatch(categories::contains)).filter(report -> startDate == null || !report.getDate().toLocalDate().isBefore(startDate)).filter(report -> endDate == null || !report.getDate().toLocalDate().isAfter(endDate)).filter(report -> {
            if (lat == null || lng == null || radius == null) return true;
            double distance = calculateDistance(report.getLocation().getY(), report.getLocation().getX(), lat, lng);
            return distance <= radius;
        }).collect(Collectors.toList());
    }

    // Devuelve la lista de IDs de usuarios que marcaron el reporte como importante
    public List<String> getLikedBy(String reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
        return new ArrayList<>(report.getLikedBy());
    }

    // Devuelve la lista de IDs de reportes que un usuario ha marcado como importantes
    public List<String> getLikedReports(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return new ArrayList<>(user.getLikedReports());
    }

    private void notifyUserAboutRejection(String userId, String reportId, String reason) {
        // Puede ser email, notificación push, etc.
        log.info("Notificando al usuario {} sobre el rechazo del reporte {}. Razón: {}", userId, reportId, reason);

        // Ejemplo con WebSocket:
        String destination = "/queue/report-rejections";
        String message = String.format("Tu reporte %s fue rechazado. Razón: %s. Tienes 5 días para corregirlo.", reportId, reason);
        messagingTemplate.convertAndSendToUser(userId, destination, message);
    }

    private void validateResubmissionDeadline(Report report) {
        if (report.getStatus() == ReportStatus.REJECTED && LocalDateTime.now().isAfter(report.getResubmissionDeadline())) {
            throw new ForbiddenActionException("El plazo para reenviar este reporte ha expirado");
        }
    }

    private void validateStatusPermissions(Report report, ReportStatus newStatus, User user) {
        boolean isOwner = report.getIdUser().equals(user.getId());

        // Usuarios regulares solo pueden cambiar a RESOLVED
        if (user.getRole() != Role.ADMIN && !(isOwner && newStatus == ReportStatus.RESOLVED)) {
            throw new AccessDeniedException("No tienes permisos para realizar esta acción");
        }
    }

    private void validateStateRequirements(Report report, ReportStatus newStatus, String rejectionReason, User user) {
        switch (newStatus) {
            case REJECTED:
                if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                    throw new IllegalArgumentException("Se requiere un motivo de rechazo");
                }
                if (report.getStatus() != ReportStatus.PENDING && report.getStatus() != ReportStatus.VERIFIED) {
                    throw new BusinessException("Solo se pueden rechazar reportes en estado PENDING o VERIFIED");
                }
                break;

            case VERIFIED:
                if (report.getStatus() != ReportStatus.PENDING) {
                    throw new BusinessException("Solo se pueden verificar reportes en estado PENDING");
                }
                break;

            case RESOLVED:
                if (report.getStatus() == ReportStatus.REJECTED) {
                    throw new BusinessException("No se puede resolver un reporte rechazado");
                }
                break;

            default:
                break;
        }
    }

    private void setStatusSpecificData(Report report, ReportStatus status, String rejectionReason, User user) {
        switch (status) {
            case REJECTED:
                report.setRejectionReason(rejectionReason);
                report.setRejectionDate(LocalDateTime.now());
                report.setResubmissionDeadline(LocalDateTime.now().plusDays(5));
                break;

            case VERIFIED:
                report.setVerifiedBy(user.getId());
                report.setVerificationDate(LocalDateTime.now());
                break;

            case RESOLVED:
                report.setResolutionDate(LocalDateTime.now());
                if (user.getRole() == Role.ADMIN) {
                    report.setResolvedBy(user.getId());
                }
                break;

            default:
                break;
        }
    }

    private void notifyStatusChange(String reportId, String oldStatus, String newStatus, Role userRole) {
        String destination = "/queue/status-updates";
        String message = String.format("Reporte %s cambió de %s a %s", reportId, oldStatus, newStatus);

        if (userRole == Role.ADMIN && "VERIFIED".equals(newStatus)) {
            message += " (Verificado por administrador)";
        }

        messagingTemplate.convertAndSend(destination, message);
        log.info("Notificación de cambio de estado enviada: {}", message);
    }
}