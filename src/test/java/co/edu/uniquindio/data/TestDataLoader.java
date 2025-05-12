package co.edu.uniquindio.data;

import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataLoader {

    public static Map<String, User> loadTestData(UserRepository userRepository, MongoTemplate mongoTemplate) {
        var encoder = new BCryptPasswordEncoder();
        return loadTestData(
                List.of(
                        createTestUser(
                                UUID.randomUUID().toString(), "Juan Perez", "3212132", "juan@gmail.com",
                                encoder.encode("12346Abc"), "Calle 2", "Armenia",
                                LocalDate.of(1982, 8, 27), Role.CLIENT, UserStatus.ACTIVE
                        ),
                        createTestUser(
                                UUID.randomUUID().toString(), "Carlos Pérez", "3101234567", "carlos@example.com",
                                encoder.encode("12346Abc"), "Calle 3", "Armenia",
                                LocalDate.of(1984, 10, 28), Role.CLIENT, UserStatus.ACTIVE
                        )
                ),
                userRepository,
                mongoTemplate
        );
    }

    private static User createTestUser(String id, String fullName, String phone, String email, String password,
                                       String address, String city, LocalDate birthDate,
                                       Role role, UserStatus status) {
        return new User(
                id,
                fullName,
                phone,
                email,
                password,
                address,
                birthDate,
                city,
                role,
                status,
                new GeoJsonPoint(-75.6801, 4.5350), // ubicación dummy
                true,
                LocalDateTime.now(),
                List.of(), List.of(), List.of(), List.of()
        );
    }
    public static Map<String, User> loadTestData(Collection<User> newUsers,UserRepository userRespository, MongoTemplate mongoTemplate) {
        // Borrar datos existentes para asegurar la repetibilidad de las pruebas.
        mongoTemplate.getDb().listCollectionNames()
                .forEach(mongoTemplate::dropCollection);
        return userRespository.saveAll(newUsers).stream().collect(Collectors.toMap(User::getId, usuario -> usuario));
    }
}