package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.services.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /// / Crear nueva categoría (Requiere rol ADMIN)
    ///
    /// / Retorna categoría creada con status 201
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.save(category));
    }

    /// / Obtener todas las categorías (filtradas por status si se especifica)
    ///
    /// / Retorna lista de categorías activas/inactivas o todas si no hay filtro
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(
            @RequestParam(required = false) CategoryStatus status) {
        if (status != null) {
            return ResponseEntity.ok(categoryService.findAllByStatus(status));
        }
        return ResponseEntity.ok(categoryService.findAll());
    }

    /// / Obtener solo los nombres de todas las categorías
    ///
    /// / Retorna lista de strings con nombres de categorías
    @GetMapping("/names")
    public ResponseEntity<List<String>> getCategoryNames() {
        return ResponseEntity.ok(categoryService.getAllCategoryNames());
    }

    /// / Actualizar categoría existente (Requiere rol ADMIN)
    ///
    /// / Retorna categoría actualizada
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest category) {
        return ResponseEntity.ok(categoryService.update(id, category));
    }

    /// / Eliminar categoría (Requiere rol ADMIN)
    ///
    /// / Retorna status 204 (sin contenido)
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /// / Buscar categoría por ID
    ///
    /// / Retorna detalle de la categoría encontrada
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }
}