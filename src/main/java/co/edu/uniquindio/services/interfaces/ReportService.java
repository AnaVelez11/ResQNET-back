package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.*;
import co.edu.uniquindio.model.enums.ReportStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReportResponse createReport(ReportRequest request, String userId);

    ReportResponse updateReport(ReportRequest request, String reportId, String userId);

    List<ReportResponse> getFilteredReports(
            String status,
            List<String> categories,
            int page,
            int size
    );

    ReportResponse updateReportStatus(
            String reportId,
            ReportStatus status,
            String rejectionReason,
            String userId
    );

    List<ReportResponse> getReportsWithFilters(
            ReportFilterRequest filter,
            String adminId
    );

    List<ReportResponse> getReportsByUserId(String userId);

    ReportResponse getReportById(String reportId);

    void generatePdfReport(
            String status,
            List<String> categories,
            LocalDate startDate,
            LocalDate endDate,
            Double lat,
            Double lng,
            Double radius,
            HttpServletResponse response
    ) throws IOException;


    // void toggleReportImportance(String reportId, String userId);
}