package co.edu.uniquindio.controllers.unit;


import co.edu.uniquindio.controllers.ReportController;
import co.edu.uniquindio.dto.LocationDTO;
import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.services.interfaces.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportControllerTest {

    @InjectMocks
    private ReportController reportController;

    @Mock
    private ReportService reportService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateReportSuccess() throws Exception {
        // Arrange
        String title = "Hueco en la vía";
        String description = "Hueco grande frente al parque";
        String userId = "user123"; // Supongamos que se obtiene por otro medio
        LocationDTO location = new LocationDTO(-75.68, 4.53);
        List<String> categories = List.of("cat1", "cat2");

        String locJson = objectMapper.writeValueAsString(location);
        String catsJson = objectMapper.writeValueAsString(categories);

        MockMultipartFile image = new MockMultipartFile("images", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

        ReportRequest expectedRequest = ReportRequest.builder()
                .title(title)
                .description(description)
                .location(location)
                .categories(categories)
                .images(List.of(image))
                .build();

        GeoJsonPoint loc = new GeoJsonPoint(location.longitude(),location.latitude());
        ReportResponse expectedResponse = ReportResponse.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .userId(userId)
                .status(String.valueOf(ReportStatus.PENDING))
                .location(loc)
                .categories(categories)
                .imageUrls(List.of("fake.jpg"))
                .build();

        when(reportService.createReport(any(), eq(userId))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> response = reportController.createReport(
                title,
                description,
                locJson,
                catsJson,
                List.of(image),
                userId
        );

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        ReportResponse body = (ReportResponse) response.getBody();
        assertNotNull(body);
        assertEquals(title, body.title());
        assertEquals(description, body.description());
        assertEquals(userId, body.userId());
        verify(reportService, times(1)).createReport(any(ReportRequest.class), eq(userId));
    }


    @Test
    void testGetReportByIdSuccess() {
        // Arrange
        String reportId = UUID.randomUUID().toString();
        ReportResponse mockResponse = ReportResponse.builder()
                .id(reportId)
                .title("Título")
                .description("Descripción")
                .userId("usuario123")
                .status(String.valueOf(ReportStatus.PENDING))
                .build();

        when(reportService.getReportById(reportId)).thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = reportController.getReportById(reportId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        ReportResponse body = (ReportResponse) response.getBody();
        assertEquals("Título", body.title());
        verify(reportService).getReportById(reportId);
    }

    @Test
    void testGetReportByIdNotFound() {
        // Arrange
        String invalidId = UUID.randomUUID().toString();
        when(reportService.getReportById(invalidId)).thenThrow(new RuntimeException("Report not found"));

        // Act
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reportController.getReportById(invalidId);
        });

        // Assert
        assertEquals("Report not found", exception.getMessage());
        verify(reportService).getReportById(invalidId);
    }
}

