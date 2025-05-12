package co.edu.uniquindio.controllers.unit;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.exceptions.BusinessException;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.CategoryMapper;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.services.implementations.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

class CategoryControllerTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock private CategoryRepository categoryRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private CategoryMapper categoryMapper;

    private Category existing;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        existing = Category.builder()
                .idCategory("id1")
                .name("Existente")
                .description("D")
                .status(CategoryStatus.ACTIVE)
                .build();
        given(categoryRepository.findByIdCategoryAndStatus("id1", CategoryStatus.ACTIVE))
                .willReturn(Optional.of(existing));
    }

    @Test
    void saveSuccess() {
        var req = new CategoryRequest("Nueva","Desc");
        var toSave = Category.builder().name("Nueva").description("Desc").build();
        var saved = toSave.toBuilder()
                .idCategory("id2")
                .status(CategoryStatus.ACTIVE)
                .build();
        var resp = new CategoryResponse("id2","Nueva","Desc",CategoryStatus.ACTIVE);

        given(categoryRepository.existsByNameAndStatus("Nueva", CategoryStatus.ACTIVE)).willReturn(false);
        given(categoryMapper.parseOf(req)).willReturn(toSave);
        given(categoryRepository.save(toSave)).willReturn(saved);
        given(categoryMapper.toCategoryResponse(saved)).willReturn(resp);

        CategoryResponse out = categoryService.save(req);
        assertThat(out).isEqualTo(resp);
    }

    @Test
    void saveThrowsOnDuplicate() {
        given(categoryRepository.existsByNameAndStatus("Dup", CategoryStatus.ACTIVE)).willReturn(true);
        assertThrows(ValueConflictException.class,
                () -> categoryService.save(new CategoryRequest("Dup","D")));
    }

    @Test
    void updateSuccessWhenNameUnchanged() {
        var req = new CategoryRequest("Existente","NuevaD");
        var updated = existing.toBuilder().description("NuevaD").build();
        var resp = new CategoryResponse("id1","Existente","NuevaD",CategoryStatus.ACTIVE);

        given(categoryRepository.save(existing)).willReturn(updated);
        given(categoryMapper.toCategoryResponse(updated)).willReturn(resp);

        CategoryResponse out = categoryService.update("id1", req);
        assertThat(out.description()).isEqualTo("NuevaD");
    }

    @Test
    void updateThrowsWhenNotFound() {
        given(categoryRepository.findByIdCategoryAndStatus("no", CategoryStatus.ACTIVE))
                .willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update("no", new CategoryRequest("A","B")));
    }

    @Test
    void deleteByIdThrowsWhenInUse() {
        given(categoryRepository.findById("id1")).willReturn(Optional.of(existing));
        given(reportRepository.existsByCategoriesContaining("id1")).willReturn(true);
        given(reportRepository.countReportsUsingCategory("id1")).willReturn(3L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.deleteById("id1"));
        assertThat(ex.getMessage()).contains("3 reporte");
    }

    @Test
    void deleteByIdLogicalWhenNotInUse() {
        given(categoryRepository.findById("id1")).willReturn(Optional.of(existing));
        given(reportRepository.existsByCategoriesContaining("id1")).willReturn(false);

        categoryService.deleteById("id1");
        then(categoryRepository).should().save(argThat(cat ->
                cat.getStatus() == CategoryStatus.DELETED
        ));
    }

    @Test
    void getAllCategoryNamesAndFindAllByStatus() {
        var c2 = existing.toBuilder().idCategory("id2").name("Otra").build();
        given(categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)).willReturn(List.of(existing, c2));

        List<String> names = categoryService.getAllCategoryNames();
        assertThat(names).containsExactly("Existente", "Otra");

        categoryService.findAllByStatus(CategoryStatus.ACTIVE);
        then(categoryMapper).should(times(2)).toCategoryResponse(any());
    }
}
