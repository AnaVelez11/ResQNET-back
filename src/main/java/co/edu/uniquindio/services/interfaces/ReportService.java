package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.*;
import co.edu.uniquindio.model.enums.ReportStatus;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReportResponse createReport(ReportRequest request, String userId);

    ReportResponse updateReport(ReportRequest request, String reportId, String userId);

    void deleteReport(String reportId, String userId);

    ReportResponse updateReportStatus(String reportId, ReportStatus status, String rejectionReason, String userId);

    ReportResponse rejectReportWithReason(String reportId, String rejectionReason, String adminId);

    ReportResponse resubmitReport(ReportRequest request, String reportId, String userId);

    List<ReportResponse> getReportsWithFilters(ReportFilterRequest filter, String adminId);

    List<ReportResponse> getReportsByUserId(String userId);

    ReportResponse getReportById(String reportId);

    List<String> getLikedReports(String userId);

    List<String> getLikedBy(String reportId);

    void toggleReportImportance(String reportId, String userId);

    void generatePdfReport(String status, List<String> categories, LocalDate startDate, LocalDate endDate, Double lat, Double lng, Double radius, HttpServletResponse response) throws IOException;


    List<ReportResponse> getAllReports();
}