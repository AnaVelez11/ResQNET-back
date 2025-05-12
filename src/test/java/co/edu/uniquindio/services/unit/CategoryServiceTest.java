package co.edu.uniquindio.services.unit;

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
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ReportRepository reportRepository;

    // Usamos el mapper real de MapStruct
    private CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        // inyecta el mapper real
        categoryService = new CategoryServiceImpl(categoryRepository, reportRepository, categoryMapper);
    }

    @Test
    void saveSuccess() {
        var req = new CategoryRequest("Cat A", "Desc A");
        var toSave = new Category();
        toSave.setName("Cat A");
        toSave.setDescription("Desc A");
        toSave.setStatus(CategoryStatus.ACTIVE);

        // simula que no existe duplicado
        given(categoryRepository.existsByNameAndStatus("Cat A", CategoryStatus.ACTIVE)).willReturn(false);
        // repo guarda y asigna id
        willAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setIdCategory("id123");
            return c;
        }).given(categoryRepository).save(any());

        CategoryResponse res = categoryService.save(req);

        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo("id123");
        assertThat(res.name()).isEqualTo("Cat A");
        then(categoryRepository).should().save(any(Category.class));
    }

    @Test
    void saveThrowsOnDuplicate() {
        given(categoryRepository.existsByNameAndStatus("Dup", CategoryStatus.ACTIVE)).willReturn(true);
        assertThrows(ValueConflictException.class,
                () -> categoryService.save(new CategoryRequest("Dup", "X")));
    }

    @Test
    void updateSuccess() {
        var existing = new Category();
        existing.setIdCategory("cid");
        existing.setName("Old");
        existing.setDescription("oldDesc");
        existing.setStatus(CategoryStatus.ACTIVE);

        given(categoryRepository.findByIdCategoryAndStatus("cid", CategoryStatus.ACTIVE))
                .willReturn(Optional.of(existing));
        // nombre distinto => comprueba duplicados
        given(categoryRepository.existsByNameAndStatus("New", CategoryStatus.ACTIVE)).willReturn(false);
        // guardado
        given(categoryRepository.save(existing)).willReturn(existing);

        var req = new CategoryRequest("New", "newDesc");
        var out = categoryService.update("cid", req);

        assertThat(out.name()).isEqualTo("New");
        assertThat(out.description()).isEqualTo("newDesc");
    }

    @Test
    void updateThrowsNotFound() {
        given(categoryRepository.findByIdCategoryAndStatus("no", CategoryStatus.ACTIVE))
                .willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.update("no", new CategoryRequest("X","Y")));
    }

    @Test
    void findAllAndFindById() {
        var cat = new Category();
        cat.setIdCategory("cid");
        cat.setName("N");
        cat.setDescription("D");
        cat.setStatus(CategoryStatus.ACTIVE);

        given(categoryRepository.findAllByStatus(CategoryStatus.ACTIVE))
                .willReturn(List.of(cat));
        given(categoryRepository.findByIdCategoryAndStatus("cid", CategoryStatus.ACTIVE))
                .willReturn(Optional.of(cat));

        var all = categoryService.findAll();
        assertThat(all).extracting(CategoryResponse::id).containsExactly("cid");

        var byId = categoryService.findById("cid");
        assertThat(byId.name()).isEqualTo("N");
    }

    @Test
    void deleteByIdLogicalAndThrowsWhenInUse() {
        var cat = new Category();
        cat.setIdCategory("c1");
        cat.setName("C1");
        cat.setStatus(CategoryStatus.ACTIVE);

        given(categoryRepository.findById("c1")).willReturn(Optional.of(cat));
        // simulamos en uso
        given(reportRepository.existsByCategoriesContaining("c1")).willReturn(true);
        given(reportRepository.countReportsUsingCategory("c1")).willReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.deleteById("c1"));
        assertThat(ex.getMessage()).contains("5 reporte");

        // cuando no está en uso
        reset(reportRepository);
        given(categoryRepository.findById("c1")).willReturn(Optional.of(cat));
        given(reportRepository.existsByCategoriesContaining("c1")).willReturn(false);
        categoryService.deleteById("c1");

        assertThat(cat.getStatus()).isEqualTo(CategoryStatus.DELETED);
        then(categoryRepository).should().save(cat);
    }

    @Test
    void getAllCategoryNames() {
        var cat = new Category();
        cat.setName("Z");
        cat.setStatus(CategoryStatus.ACTIVE);
        given(categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)).willReturn(List.of(cat));

        var names = categoryService.getAllCategoryNames();
        assertThat(names).containsExactly("Z");
    }
}
