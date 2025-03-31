package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface CategoryServices {
    CategoryResponse save(CategoryRequest category);
    CategoryResponse update(String id,CategoryRequest category);
    List<CategoryResponse> findAll();
    CategoryResponse findById(String id);
    void deleteById(String id);
}
