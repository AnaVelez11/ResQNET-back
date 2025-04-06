package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsById(String id);
    Optional<User> findByEmail(String email);

}
    