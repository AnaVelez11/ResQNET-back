package co.edu.uniquindio.repositories;

import co.edu.uniquindio.dto.UserResponse;
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

    @Query(value = "{ 'status': { $ne: 'DELETED' }, 'email': ?0 }")
    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);


    @Query(value = "{ 'status': { $ne: 'DELETED' }, " +
            "  'fullName': { $regex: ?0, $options: 'i' }, " +
            "  'email': { $regex: ?1, $options: 'i' }, " +
            "  ?#{ [2] != null ? 'dateBirth' : '_ignore' } : ?2 }",
            sort = "{ 'fullName': 1 }")
    Page<User> findExistingUsersByFilters(String fullName, String email, LocalDate dateBirth, Pageable pageable);

    List<UserResponse> findByStatusNot(UserStatus status);

    @Query("{ 'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } }, 'id': { $ne: ?3 } }")
    List<User> findUsersNearLocation(double longitude, double latitude, double maxDistanceInMeters, String excludeUserId);

    @Query("{ 'email': ?0, 'active': true }")
        // Filtra solo usuarios activos
    Optional<User> findActiveByEmail(String email);

}
    