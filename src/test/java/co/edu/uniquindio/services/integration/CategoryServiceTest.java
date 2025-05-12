package co.edu.uniquindio.services.integration;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.exceptions.BusinessException;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.services.interfaces.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CategoryServiceTest {

    @Autowired private CategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Limpiar y pre-cargar una categoría
        mongoTemplate.dropCollection(Category.class);
        categoryRepository.save(Category.builder()
                .idCategory("base")
                .name("Base")
                .description("Categoría base")
                .status(CategoryStatus.ACTIVE)
                .build()
        );
    }

    @Test
    void saveSuccess() {
        var req = new CategoryRequest("Nueva", "Desc nueva");
        CategoryResponse res = categoryService.save(req);

        assertThat(res.id()).isNotNull();
        assertThat(res.name()).isEqualTo("Nueva");
        assertThat(categoryRepository.findByIdCategoryAndStatus(res.id(), CategoryStatus.ACTIVE))
                .isPresent();
    }

    @Test
    void saveThrowsOnDuplicateName() {
        var req = new CategoryRequest("Base", "Cualquier");
        assertThrows(ValueConflictException.class, () -> categoryService.save(req));
    }

    @Test
    void updateSuccess() {
        var orig = categoryRepository.findByName("Base").orElseThrow();
        var req = new CategoryRequest("BaseRenombrada", "Desc mod");
        CategoryResponse updated = categoryService.update(orig.getIdCategory(), req);

        assertThat(updated.name()).isEqualTo("BaseRenombrada");
        assertThat(categoryRepository.findByIdCategoryAndStatus(orig.getIdCategory(), CategoryStatus.ACTIVE))
                .map(Category::getDescription)
                .contains("Desc mod");
    }

    @Test
    void updateThrowsOnNotFound() {
        var req = new CategoryRequest("X", "Y");
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update(UUID.randomUUID().toString(), req));
    }

    @Test
    void findAllAndFindById() {
        List<CategoryResponse> all = categoryService.findAll();
        assertThat(all).hasSize(1)
                .first().extracting(CategoryResponse::name).isEqualTo("Base");

        String id = all.get(0).id();
        CategoryResponse byId = categoryService.findById(id);
        assertThat(byId.name()).isEqualTo("Base");
    }

    @Test
    void deleteByIdLogicalAndThrowsWhenInUse() {
        // Preparo categoría en uso
        Category c = categoryRepository.save(Category.builder()
                .idCategory("c1")
                .name("C1")
                .description("Desc")
                .status(CategoryStatus.ACTIVE)
                .build()
        );
        reportRepository.save(co.edu.uniquindio.model.Report.builder()
                .title("r")
                .description("d")
                .date(java.time.LocalDateTime.now())
                .ratingsImportant(0)
                .status(co.edu.uniquindio.model.enums.ReportStatus.PENDING)
                .location(new org.springframework.data.mongodb.core.geo.GeoJsonPoint(0,0))
                .idUser("u")
                .categories(List.of("c1"))
                .imageUrls(List.of())
                .build()
        );

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.deleteById("c1"));
        assertThat(ex.getMessage()).contains("usada por");

        reportRepository.deleteAll();
        categoryService.deleteById("c1");
        assertThat(categoryRepository.findByIdCategoryAndStatus("c1", CategoryStatus.DELETED))
                .isPresent();
    }

    @Test
    void getAllCategoryNames() {
        List<String> names = categoryService.getAllCategoryNames();
        assertThat(names).containsExactly("Base");
    }
}
