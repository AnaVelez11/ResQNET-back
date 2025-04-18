package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.model.enums.CategoryStatus;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface CategoryService {
    CategoryResponse save(CategoryRequest category);
    CategoryResponse update(String id,CategoryRequest category);
    List<CategoryResponse> findAll();
    CategoryResponse findById(String id);
    void deleteById(String id);
    List<String> getAllCategoryNames();
    List<CategoryResponse> findAllByStatus(CategoryStatus status);



}
