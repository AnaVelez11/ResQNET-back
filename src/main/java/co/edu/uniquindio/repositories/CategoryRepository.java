package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByName(String name);
    List<Category> findByStatusNot(CategoryStatus status);
}