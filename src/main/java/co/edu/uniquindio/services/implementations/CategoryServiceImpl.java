package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.CategoryMapper;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse save(CategoryRequest category) {
        var newCategory = categoryMapper.parseOf(category);
        validateCategoryName(category.name());
        return categoryMapper.toCategoryResponse(
                categoryRepository.save(newCategory)
        );
    }

    @Override
    public CategoryResponse update(String id,CategoryRequest category) {
        var updatedCategory = findCategoryById(id);
        updatedCategory.setName(category.name());
        if( !updatedCategory.getName().equals(category.name()) ){
            validateCategoryName(category.name());
        }
        updatedCategory.setDescription(category.description());
        return categoryMapper.toCategoryResponse(
                categoryRepository.save(updatedCategory)
        );
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse findById(String id) {
        return categoryMapper.toCategoryResponse(findCategoryById(id));
    }

    @Override
    public void deleteById(String id) {
        var categoryStored = findCategoryById(id);
        categoryStored.setStatus(CategoryStatus.DELETED);
        categoryRepository.save(categoryStored);
    }

    private Category findCategoryById(String id){
        var storedCategory = categoryRepository.findById(id);
//        if(storedCategory.isEmpty()) {
//            throw new ResourceNotFoundException();
//        }
        return storedCategory.orElseThrow(ResourceNotFoundException::new);
    }

    private void validateCategoryName(String categoryName) {
        var category = categoryRepository.findByName(categoryName);
        if(category.isPresent()) {
            throw new ValueConflictException("Category name already exists");
        }
    }
}
