package co.edu.uniquindio.repositories;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.model.enums.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Category catActive1;
    private Category catActive2;
    private Category catDeleted;

    @BeforeEach
    void setUp() {
        // Limpiar colecciones
        mongoTemplate.dropCollection(Category.class);
        mongoTemplate.dropCollection(Report.class);

        // Crear categorías de prueba
        catActive1 = categoryRepository.save(Category.builder()
                .idCategory("c1")
                .name("Cat1")
                .description("Desc1")
                .status(CategoryStatus.ACTIVE)
                .build());
        catActive2 = categoryRepository.save(Category.builder()
                .idCategory("c2")
                .name("Cat2")
                .description("Desc2")
                .status(CategoryStatus.ACTIVE)
                .build());
        catDeleted = categoryRepository.save(Category.builder()
                .idCategory("c3")
                .name("Cat3")
                .description("Desc3")
                .status(CategoryStatus.DELETED)
                .build());

        // Crear un reporte que use catActive1
        reportRepository.save(Report.builder()
                .title("R1")
                .description("D1")
                .date(LocalDateTime.now())
                .ratingsImportant(0)
                .status(ReportStatus.PENDING)
                .location(new GeoJsonPoint(0,0))
                .idUser("u1")
                .categories(List.of("c1"))
                .imageUrls(List.of())
                .build());
    }

    @Test
    void testFindByName() {
        Optional<Category> found = categoryRepository.findByName("Cat2");
        assertTrue(found.isPresent());
        assertThat(found.get().getIdCategory()).isEqualTo("c2");
    }

    @Test
    void testFindByStatusNot() {
        List<Category> notDeleted = categoryRepository.findByStatusNot(CategoryStatus.DELETED);
        // Debe traer solo las activas (c1, c2)
        assertThat(notDeleted)
                .extracting(Category::getIdCategory)
                .containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void testExistsByNameAndStatus() {
        assertTrue(categoryRepository.existsByNameAndStatus("Cat1", CategoryStatus.ACTIVE));
        // Cat3 existe pero está DELETED, así que esto debe ser false
        assertThat(categoryRepository.existsByNameAndStatus("Cat3", CategoryStatus.ACTIVE)).isFalse();
    }

    @Test
    void testFindByIdCategoryAndStatus() {
        Optional<Category> foundActive = categoryRepository.findByIdCategoryAndStatus("c1", CategoryStatus.ACTIVE);
        assertTrue(foundActive.isPresent());

        Optional<Category> foundDeleted = categoryRepository.findByIdCategoryAndStatus("c3", CategoryStatus.ACTIVE);
        assertThat(foundDeleted).isEmpty();
    }

    @Test
    void testFindAllByStatus() {
        List<Category> actives = categoryRepository.findAllByStatus(CategoryStatus.ACTIVE);
        assertThat(actives)
                .hasSize(2)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Cat1", "Cat2");
    }

    @Test
    void testExistsByCategoriesContaining_viaReportRepository() {
        // ReportRepository sí está apuntando a la colección 'reports'
        assertTrue(reportRepository.existsByCategoriesContaining("c1"));
        assertThat(reportRepository.existsByCategoriesContaining("c2")).isFalse();
        assertThat(reportRepository.existsByCategoriesContaining("c3")).isFalse();
    }
}
