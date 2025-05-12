package co.edu.uniquindio.repositories;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.GeospatialIndex;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest(properties = "spring.data.mongodb.auto-index-creation=true")
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    private Map<String, User> users;
    private String userId;

    @BeforeEach
    void setUp() {

        mongoTemplate.dropCollection(Report.class);
        // 1) Carga usuarios de prueba
        users = TestDataLoader.loadTestData(userRepository, mongoTemplate);
        userId = users.values().iterator().next().getId();

        // 2) Limpia la colección de reportes
        reportRepository.deleteAll();

        // 3) Inserta algunos reportes variados
        reportRepository.saveAll(List.of(
                Report.builder()
                        .title("R1")
                        .description("Desc1")
                        .date(LocalDateTime.now().minusHours(1))
                        .ratingsImportant(0)
                        .status(ReportStatus.PENDING)
                        .location(new GeoJsonPoint(-75.68, 4.53))
                        .idUser(userId)
                        .categories(List.of("cat1"))
                        .imageUrls(List.of())
                        .build(),

                Report.builder()
                        .title("R2")
                        .description("Desc2")
                        .date(LocalDateTime.now().minusDays(2))
                        .ratingsImportant(0)
                        .status(ReportStatus.PENDING)
                        .location(new GeoJsonPoint(-75.6805, 4.5335))
                        .idUser(userId)
                        .categories(List.of("cat2"))
                        .imageUrls(List.of())
                        .build(),

                Report.builder()
                        .title("R3")
                        .description("Desc3")
                        .date(LocalDateTime.now().minusHours(2))
                        .ratingsImportant(0)
                        .status(ReportStatus.RESOLVED)
                        .location(new GeoJsonPoint(-75.68, 4.53))
                        .idUser(userId)
                        .categories(List.of("cat1"))
                        .imageUrls(List.of())
                        .build()
        ));
    }

    @Test
    void testFindByStatusAndCategoriesIn() {
        Page<Report> page = reportRepository.findByStatusAndCategoriesIn(
                ReportStatus.PENDING,
                List.of("cat1"),
                PageRequest.of(0, 10)
        );
        // Solo R1 cumple (estado PENDING y categoría cat1)
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("R1");
    }

    @Test
    void testFindByStatus() {
        Page<Report> page = reportRepository.findByStatus(
                ReportStatus.PENDING,
                PageRequest.of(0, 10)
        );
        // R1 y R2 son PENDING
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testFindByCategoriesIn() {
        Page<Report> page = reportRepository.findByCategoriesIn(
                List.of("cat1", "cat2"),
                PageRequest.of(0, 10)
        );
        // R1 (cat1), R2 (cat2), R3 (cat1)
        assertThat(page.getTotalElements()).isEqualTo(3);
    }


    @Test
    void testExistsByCategoriesContainingAndCountReportsUsingCategory() {
        // "cat1" aparece en R1 y R3
        assertThat(reportRepository.existsByCategoriesContaining("cat1")).isTrue();
        assertThat(reportRepository.countReportsUsingCategory("cat1")).isEqualTo(2);

        // "no-cat" no aparece
        assertThat(reportRepository.existsByCategoriesContaining("no-cat")).isFalse();
        assertThat(reportRepository.countReportsUsingCategory("no-cat")).isEqualTo(0);
    }

    @Test
    void testFindAllActive() {
        // Todos los que cargamos tienen anonymous=false (valor por defecto)
        var page = reportRepository.findAllActive(PageRequest.of(0, 10));
        // Debe traer los 3
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void testFindByIdUser() {
        List<Report> byUser = reportRepository.findByIdUser(userId);
        // Los tres reportes usan el mismo idUser
        assertThat(byUser).hasSize(3);
        assertThat(byUser)
                .allMatch(r -> r.getIdUser().equals(userId));
    }
}
