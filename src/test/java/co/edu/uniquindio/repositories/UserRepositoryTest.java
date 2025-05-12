// src/test/java/co/edu/uniquindio/repositories/UserRepositoryTest.java
package co.edu.uniquindio.repositories;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        users = TestDataLoader.loadTestData(userRepository, mongoTemplate);
    }

    @Test
    void testFindUserByEmailSuccess() {
        var testUser = users.values().iterator().next();
        Optional<User> result = userRepository.findByEmail(testUser.getEmail());

        assertTrue(result.isPresent());
        assertThat(result.get().getName()).isEqualTo(testUser.getName());
    }

    @Test
    void testFindUserByEmailWhenInactive() {
        User testUser = users.values().iterator().next();
        testUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(testUser);

        Optional<User> result = userRepository.findByEmail(testUser.getEmail());
        // INACTIVE is not filtered out by the repository (only DELETED), so it should still be retrievable
        assertTrue(result.isPresent());
    }

    @Test
    void testFindExistingUsersByFilters() {
        User testUser = users.values().iterator().next();
        LocalDate birthDate = testUser.getBirthDate();

        Page<User> result = userRepository.findExistingUsersByFilters(
                testUser.getName(),
                testUser.getEmail(),
                birthDate,
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void testFindByStatusNotInactive() {
        List<User> result = userRepository.findByStatusNot(UserStatus.INACTIVE);

        // All users loaded in TestDataLoader are ACTIVE
        assertThat(result).hasSize(users.size());
        assertThat(result)
                .extracting(User::getId)
                .containsExactlyInAnyOrderElementsOf(users.keySet());
    }
}
