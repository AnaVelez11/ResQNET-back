package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    boolean existsById(String id);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    // Método específico para encontrar solo usuarios no inactivos
    @Query(value = "{ 'status': { $ne: 'INACTIVE' }, 'email': ?0 }")
    Optional<User> findActiveUserByEmail(String email);

    @Query(value = "{ 'status': { $ne: 'INACTIVE' }, " +
            "  'name': { $regex: ?0, $options: 'i' }, " +
            "  'email': { $regex: ?1, $options: 'i' }, " +
            "  ?#{ [2] != null ? 'birthDate' : '_ignore' } : ?2 }",
            sort = "{ 'name': 1 }")
    Page<User> findExistingUsersByFilters(String fullName, String email, LocalDate birthDate, Pageable pageable);

    List<User> findByStatusNot(UserStatus status);

    @Query("{ 'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'id': { $ne: ?3 } }")
    List<User> findUsersNearLocation(double longitude, double latitude, double maxDistanceInMeters, String excludeUserId);


}
    