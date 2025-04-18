package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface    CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByName(String name);

    List<Category> findByStatusNot(CategoryStatus status);

    boolean existsByNameAndStatus(String name, CategoryStatus status);

    Optional<Category> findByIdCategoryAndStatus(String id, CategoryStatus status);

    List<Category> findAllByStatus(CategoryStatus status);


    @Query("{ 'categories': ?0 }")
    boolean existsByCategoriesContaining(String categoryId);
}