package co.edu.uniquindio.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NearbyReportNotification {
    private String reportId;
    private String title;
    private double distance;
    private List<String> categories;
    private String timestamp;
}