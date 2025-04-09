package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.model.enums.ReportStatus;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(ReportRequest request, String userId);
    List<ReportResponse> getFilteredReports(String status, List<String> categories, int page, int size);
    void incrementRatings(String reportId);
    ReportResponse updateReportStatus(String reportId, ReportStatus status, String rejectionReason);
}
