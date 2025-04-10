package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.CloudinaryService;
import co.edu.uniquindio.services.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
   private final ReportRepository reportRepository;
   private final UserRepository userRepository;
   private final CategoryRepository categoryRepository;
   private final CloudinaryService cloudinaryService;

   @Override
   public ReportResponse createReport(ReportRequest request, String userId) {
      // 1. Validar usuario
      userRepository.findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // 2. Validar categorías
      if (request.categories() != null) {
         request.categories().forEach(categoryId ->
                 categoryRepository.findById(categoryId)
                         .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + categoryId))
         );
      }

      // 3. Subir imágenes a Cloudinary (si existen)
      List<String> imageUrls = null;
      if (request.images() != null && !request.images().isEmpty()) {
         imageUrls = cloudinaryService.uploadImages(request.images());
      }

      // 4. Crear y guardar el reporte
      Report report = Report.builder()
              .title(request.title())
              .description(request.description())
              .location(request.location())
              .categories(request.categories())
              .idUser(new ObjectId(userId))
              .status(ReportStatus.PENDING)
              .date(LocalDateTime.now())
              .ratingsImportant(0)
              .imageUrls(imageUrls)
              .build();

      Report savedReport = reportRepository.save(report);

      // 5. Convertir a DTO y retornar
      return convertToResponse(savedReport);
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
              .map(this::convertToResponse)
              .collect(Collectors.toList());
   }

   @Override
   public void incrementRatings(String reportId) {
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
      report.setRatingsImportant(report.getRatingsImportant() + 1);
      reportRepository.save(report);
   }

   @Override
   public ReportResponse updateReportStatus(String reportId, ReportStatus status, String rejectionReason) {
      Report report = reportRepository.findById(reportId)
              .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

      report.setStatus(status);
      if (status == ReportStatus.REJECTED) {
         report.setRejectionReason(rejectionReason);  // Campo adicional en la clase Report
      }

      Report updatedReport = reportRepository.save(report);
      return convertToResponse(updatedReport);
   }

   // --- Métodos auxiliares ---
   private ReportResponse convertToResponse(Report report) {
      return ReportResponse.builder()
              .id(report.getId())
              .title(report.getTitle())
              .description(report.getDescription())
              .location(report.getLocation())
              .categories(report.getCategories())
              .status(report.getStatus().name())
              .ratingsImportant(report.getRatingsImportant())
              .userId(report.getIdUser().toString())
              .imageUrls(report.getImageUrls())
              .date(report.getDate().toString())
              .rejectionReason(report.getRejectionReason())
              .build();
   }

}