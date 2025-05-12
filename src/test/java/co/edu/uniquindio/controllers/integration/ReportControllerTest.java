package co.edu.uniquindio.controllers.integration;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.dto.LocationDTO;
import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MongoTemplate mongoTemplate;

    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        // 1) Cargar usuarios
        users = TestDataLoader.loadTestData(userRepository, mongoTemplate);

        // 2) Limpiar colecciones
        reportRepository.deleteAll();
        categoryRepository.deleteAll();

        // 3) Precargar categorías
        categoryRepository.save(Category.builder()
                .idCategory("cat1")
                .name("Basura")
                .description("Reportes de basura")
                .status(CategoryStatus.ACTIVE)
                .build()
        );
        categoryRepository.save(Category.builder()
                .idCategory("cat2")
                .name("Infraestructura")
                .description("Reportes de infraestructuras")
                .status(CategoryStatus.ACTIVE)
                .build()
        );
    }

    @Test
    void testCreateReportSuccess() throws Exception {
        var user = users.values().iterator().next();

        String locJson  = objectMapper.writeValueAsString(new LocationDTO(-75.68, 4.53));
        String catsJson = objectMapper.writeValueAsString(List.of("cat1", "cat2"));
        MockMultipartFile image = new MockMultipartFile(
                "images", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes()
        );

        mockMvc.perform(multipart("/api/reports/create")
                        .file(image)
                        .param("title", "Hueco en la vía")
                        .param("description", "Hueco grande frente al parque")
                        .param("location", locJson)
                        .param("categories", catsJson)
                        .param("userId", user.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hueco en la vía"))
                .andExpect(jsonPath("$.description").value("Hueco grande frente al parque"))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.status").value(ReportStatus.PENDING.toString()));
    }

    @Test
    void testGetReportByIdSuccess() throws Exception {
        Report rpt = reportRepository.save(Report.builder()
                .title("Título")
                .description("Desc")
                .date(LocalDateTime.now())
                .ratingsImportant(0)
                .status(ReportStatus.PENDING)
                .location(new GeoJsonPoint(-75.68, 4.53))
                .idUser("usuario-prueba")
                .categories(List.of("cat1"))
                .imageUrls(List.of())
                .build()
        );

        mockMvc.perform(get("/api/reports/" + rpt.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título"))
                .andExpect(jsonPath("$.description").value("Desc"));
    }

    @Test
    void testGetReportByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/reports/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
