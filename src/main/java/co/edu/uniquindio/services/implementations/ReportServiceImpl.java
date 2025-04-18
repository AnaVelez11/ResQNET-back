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
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.itextpdf.layout.Document;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

   private final ReportRepository reportRepository;
   private final UserRepository userRepository;
   private final CategoryRepository categoryRepository;
   private final CloudinaryService cloudinaryService;
   private final ApplicationEventPublisher eventPublisher;
   private final ReportMapper reportMapper;
   private final SimpMessagingTemplate messagingTemplate;


   @Override
   public ReportResponse createReport(ReportRequest request, String userId) {
      // 1. Validar usuario
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

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
      GeoJsonPoint geoPoint = new GeoJsonPoint(
              request.location().longitude(),
              request.location().latitude()
      );

      // 5. Crear y guardar el reporte
      Report report = Report.builder()
              .title(request.title())
              .description(request.description())
              .location(geoPoint)
              .categories(request.categories())
              .idUser(userId)
              .status(ReportStatus.PENDING)
              .date(LocalDateTime.now())
              .ratingsImportant(0)
              .imageUrls(imageUrls)
              .build();


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
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

      // 2. Verificar propiedad
      if (!report.getIdUser().equals(userId)) {
         throw new ForbiddenActionException("No tienes permisos para editar este reporte");
      }

      // 3. Validar categorías
      if (request.categories() != null) {
         List<String> invalidCategories = request.categories().stream()
                 .filter(categoryId -> !categoryRepository.existsById(categoryId))
                 .toList();

         if (!invalidCategories.isEmpty()) {
            throw new ResourceNotFoundException("Categorías no encontradas: " + String.join(", ", invalidCategories));
         }
      }

      // 4. Procesar imágenes
      List<String> imageUrls = request.images() != null && !request.images().isEmpty()
              ? cloudinaryService.uploadImages(request.images())
              : report.getImageUrls();

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
   public List<ReportResponse> getFilteredReports(
           String status,
           List<String> categories,
           int page,
           int size
   ) {
      Pageable pageable = PageRequest.of(page, size);
      Page<Report> reportsPage;

      // Filtrar por estado y categorías (si se proporcionan)
      if (status != null && categories != null) {
         reportsPage = reportRepository.findByStatusAndCategoriesIn(
                 ReportStatus.valueOf(status.toUpperCase()),
                 categories,
                 pageable
         );
      } else if (status != null) {
         reportsPage = reportRepository.findByStatus(
                 ReportStatus.valueOf(status.toUpperCase()),
                 pageable
         );
      } else if (categories != null) {
         reportsPage = reportRepository.findByCategoriesIn(categories, pageable);
      } else {
         reportsPage = reportRepository.findAll(pageable);
      }

         return reportsPage.getContent()
                 .stream()
                 .map(report -> convertToResponse(report, "Reporte obtenido correctamente"))
                 .collect(Collectors.toList());
   }

   /*
   @Override
   public void toggleReportImportance(String reportId, String userId) {
      // 1. Obtener reporte y usuario
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // 2. Verificar si el usuario ya marcó el reporte
      boolean isLiked = user.getLikedReports().contains(reportId);

      if (isLiked) {
         // 3a. Si ya está marcado: Quitar like
         user.getLikedReports().remove(reportId);
         report.getLikedBy().remove(userId);
         report.setRatingsImportant(report.getRatingsImportant() - 1);
      } else {
         // 3b. Si no está marcado: Agregar like
         user.getLikedReports().add(reportId);
         report.getLikedBy().add(userId);
         report.setRatingsImportant(report.getRatingsImportant() + 1);
      }

      // 4. Guardar cambios
      userRepository.save(user);
      reportRepository.save(report);
   }

    */

   @Override
   public ReportResponse updateReportStatus(String reportId, ReportStatus status, String rejectionReason, String userId) {
      log.info("Buscando reporte con ID: {}", reportId);

      // 1. Validar existencia del reporte y usuario
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // 2. Validar permisos (dueño o ADMIN)
      if (!user.getRole().equals(Role.ADMIN) && !report.getIdUser().equals(userId)) {
         throw new AccessDeniedException("No tienes permisos para modificar este reporte");
      }

      // 3. Validar transiciones de estado permitidas
      validateStatusTransition(report.getStatus(), status, user.getRole(), report.getIdUser().equals(userId));

      String oldStatus = report.getStatus().name();
      log.info("Actualizando estado de {} a {}", oldStatus, status);
      report.setStatus(status);

      if (status == ReportStatus.REJECTED) {
         if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Razón de rechazo es requerida");
         }
         report.setRejectionReason(rejectionReason);
      }

      Report updatedReport = reportRepository.save(report);

      notifyStatusChange(reportId, oldStatus, status.name());

      return convertToResponse(updatedReport, "Estado de reporte actualizado!");
   }



   @Override
   public List<ReportResponse> getReportsWithFilters(ReportFilterRequest filter, String adminId) {
      validateAdmin(adminId);

      List<Report> filteredReports = reportRepository.findAll().stream()
              .filter(report -> filter.categories() == null || !filter.categories().isEmpty() && report.getCategories().stream().anyMatch(filter.categories()::contains))
              .filter(report -> filter.status() == null || report.getStatus().name().equalsIgnoreCase(filter.status()))
              .filter(report -> {
                 if (filter.startDate() == null && filter.endDate() == null) return true;
                 LocalDate date = report.getDate().toLocalDate();
                 return (filter.startDate() == null || !date.isBefore(filter.startDate())) &&
                         (filter.endDate() == null || !date.isAfter(filter.endDate()));
              })
              .filter(report -> {
                 if (filter.latitude() == null || filter.longitude() == null || filter.radiusKm() == null) return true;
                 double distance = calculateDistance(
                         report.getLocation().getY(), report.getLocation().getX(),
                         filter.latitude(), filter.longitude());
                 return distance <= filter.radiusKm();
              })
              .toList();

      return filteredReports.stream()
              .map(report -> convertToResponse(report, "Filtrado exitoso"))
              .toList();

   }


   @Override
   public List<ReportResponse> getReportsByUserId(String userId) {
      // 1. Verifica que el usuario exista
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // 2. Obtiene los reportes por sus IDs (asumiendo que User tiene una lista de IDs)
      List<Report> reports = reportRepository.findAllById(user.getReports());

      // 3. Convierte los reportes a respuestas
      return reports.stream()
              .map(report -> convertToResponse(report, "Reporte del usuario"))
              .toList();
   }

   // Método para obtener un reporte por su ID
   @Override
   public ReportResponse getReportById(String reportId) {
      // Buscar el reporte por su ID
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

      // Convertir el reporte a su DTO correspondiente y retornar
      return convertToResponse(report, "Reporte actualizado correctamente.");
   }

   // --- Métodos auxiliares ---
   private ReportResponse convertToResponse(Report report, String message) {
      return ReportResponse.builder()
              .id(report.getId())
              .title(report.getTitle())
              .description(report.getDescription())
              .location(report.getLocation())
              .categories(report.getCategories())
              .status(report.getStatus() != null ? report.getStatus().name() : null)
              .ratingsImportant(report.getRatingsImportant())
              .userId(report.getIdUser())
              .imageUrls(report.getImageUrls() != null ? report.getImageUrls() : List.of())
              .date(report.getDate() != null ? report.getDate().toString() : null)
              .rejectionReason(report.getRejectionReason())
              .message(message)
              .build();

   }

   public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
      final int R = 6371; // Radio de la Tierra en km

      double latDistance = Math.toRadians(lat2 - lat1);
      double lonDistance = Math.toRadians(lon2 - lon1);

      double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
              + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
              * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

      return R * c;
   }
   private void notifyNearbyUsers(Report report, String authorId) {
      final double notificationRadiusKm = 10.0; // Radio configurable
      final double maxDistanceInMeters = notificationRadiusKm * 1000;

      try {
         // 1. Loggeo para diagnóstico
         log.info("Buscando usuarios cerca de reporte ID: {} en ubicación: {},{}",
                 report.getId(),
                 report.getLocation().getX(),
                 report.getLocation().getY());

         // 2. Buscar usuarios cercanos (con manejo de null)
         List<User> nearbyUsers = userRepository.findUsersNearLocation(
                 report.getLocation().getX(),
                 report.getLocation().getY(),
                 maxDistanceInMeters,
                 authorId
         );

         if (nearbyUsers == null || nearbyUsers.isEmpty()) {
            log.info("No se encontraron usuarios cercanos para notificar");
            return;
         }

         // 3. Procesar notificaciones
         nearbyUsers.stream()
                 .filter(user -> user.getLocation() != null)
                 .forEach(user -> {
                    try {
                       double distance = calculateDistance(
                               report.getLocation().getY(),
                               report.getLocation().getX(),
                               user.getLocation().getY(),
                               user.getLocation().getX()
                       );

                       log.debug("Notificando al usuario {} ({} km)", user.getId(), distance);

                       eventPublisher.publishEvent(
                               new NewReportEvent(this, report, user.getId(), distance)
                       );
                    } catch (Exception e) {
                       log.error("Error notificando al usuario " + user.getId(), e);
                    }
                 });

      } catch (Exception e) {
         log.error("Error en búsqueda de usuarios cercanos", e);
         // Puedes agregar métricas o alertas aquí
      }
   }
   private void validateStatusTransition(ReportStatus currentStatus, ReportStatus newStatus, Role userRole, boolean isOwner) {
      // Admins pueden hacer cualquier transición
      if (userRole.equals(Role.ADMIN)) return;

      // Dueños solo pueden cambiar a RESOLVED (y solo si el estado actual es PENDING)
      if (isOwner) {
         if (newStatus.equals(ReportStatus.RESOLVED) && currentStatus.equals(ReportStatus.PENDING)) {
            return;
         }
         throw new BusinessException("Solo puedes marcar como RESUELTO reportes en estado PENDIENTE");
      }

      throw new AccessDeniedException("Transición no permitida");
   }

   private void notifyStatusChange(String reportId, String oldStatus, String newStatus) {
      String destination = "/queue/status-updates";
      String message = String.format("Reporte %s cambió de %s a %s", reportId, oldStatus, newStatus);
      messagingTemplate.convertAndSend(destination, message);
      log.debug("Notificación enviada: {}", message);
   }
   private void validateAdmin(String userId) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      if (user.getRole() != Role.ADMIN) {
         throw new ForbiddenActionException("Solo los administradores pueden generar informes");
      }
   }
   @Override
   public void generatePdfReport(
           String status,
           List<String> categories,
           LocalDate startDate,
           LocalDate endDate,
           Double lat,
           Double lng,
           Double radius,
           HttpServletResponse response) throws IOException {

      // Usar el nuevo método filterReports
      List<Report> filteredReports = filterReports(status, categories, startDate, endDate, lat, lng, radius);

      // Configurar el PDF
      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "attachment; filename=reportes.pdf");

      // Generar el PDF
      try (PdfWriter writer = new PdfWriter(response.getOutputStream());
           PdfDocument pdf = new PdfDocument(writer);
           Document document = new Document(pdf)) {

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

   private List<Report> filterReports(
           String status,
           List<String> categories,
           LocalDate startDate,
           LocalDate endDate,
           Double lat,
           Double lng,
           Double radius) {

      // Obtener todos los reportes (o usar una consulta filtrada si tienes MongoDB/Spring Data)
      List<Report> allReports = reportRepository.findAll();

      // Filtrar manualmente con Streams
      return allReports.stream()
              .filter(report -> status == null || report.getStatus().name().equalsIgnoreCase(status))
              .filter(report -> categories == null || report.getCategories().stream().anyMatch(categories::contains))
              .filter(report -> startDate == null || !report.getDate().toLocalDate().isBefore(startDate))
              .filter(report -> endDate == null || !report.getDate().toLocalDate().isAfter(endDate))
              .filter(report -> {
                 if (lat == null || lng == null || radius == null) return true;
                 double distance = calculateDistance(
                         report.getLocation().getY(),
                         report.getLocation().getX(),
                         lat,
                         lng
                 );
                 return distance <= radius;
              })
              .collect(Collectors.toList());
   }






}