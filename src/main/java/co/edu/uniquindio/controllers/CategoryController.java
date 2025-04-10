package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.services.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryResponse create(@Valid CategoryRequest category) {
        return categoryService.save(category);
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.findAll();
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable("id") String id, @Valid CategoryRequest category) {
        return categoryService.update(id, category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") String id) {
        categoryService.deleteById(id);
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable("id") String id) {
        return categoryService.findById(id);
    }

}
