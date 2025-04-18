package co.edu.uniquindio.services.implementations;

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
import co.edu.uniquindio.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ReportRepository reportRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse save(CategoryRequest category) {
        validateCategoryName(category.name());

        Category newCategory = categoryMapper.parseOf(category);
        newCategory.setStatus(CategoryStatus.ACTIVE);

        return categoryMapper.toCategoryResponse(
                categoryRepository.save(newCategory)
        );
    }

    @Override
    public CategoryResponse update(String id, CategoryRequest category) {
        Category existingCategory = findActiveById(id);

        if(!existingCategory.getName().equals(category.name())) {
            validateCategoryName(category.name());
        }

        existingCategory.setName(category.name());
        existingCategory.setDescription(category.description());

        return categoryMapper.toCategoryResponse(
                categoryRepository.save(existingCategory)
        );
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse findById(String id) {
        return categoryMapper.toCategoryResponse(findActiveById(id));
    }

    @Override
    public void deleteById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Verificar si la categoría está en uso
        if (reportRepository.existsByCategoriesContaining(id)) {
            long usageCount = reportRepository.countReportsUsingCategory(id);
            throw new BusinessException(
                    String.format("No se puede eliminar la categoría '%s'. Está siendo usada por %d reporte(s).",
                            category.getName(), usageCount)
            );
        }

        // Eliminación lógica
        category.setStatus(CategoryStatus.DELETED);
        categoryRepository.save(category);
    }

    @Override
    public List<String> getAllCategoryNames() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE)
                .stream()
                .map(Category::getName)
                .toList();
    }

    // Métodos auxiliares
    private Category findActiveById(String id) {
        return categoryRepository.findByIdCategoryAndStatus(id, CategoryStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada o inactiva"));
    }

    private void validateCategoryName(String name) {
        if(categoryRepository.existsByNameAndStatus(name, CategoryStatus.ACTIVE)) {
            throw new ValueConflictException("Ya existe una categoría activa con este nombre");
        }
    }

    //Método para buscar por estado
    public List<CategoryResponse> findAllByStatus(CategoryStatus status) {
        return categoryRepository.findAllByStatus(status)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }
}