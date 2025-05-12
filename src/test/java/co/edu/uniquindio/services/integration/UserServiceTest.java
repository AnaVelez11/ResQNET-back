package co.edu.uniquindio.services.integration;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.implementations.UserServiceImpl;
import co.edu.uniquindio.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        users = TestDataLoader.loadTestData(userRepository, mongoTemplate);
    }

    @Test
    void testCreateUser() {
        GeoJsonPoint location = new GeoJsonPoint(-75.6801, 4.5350);

        var user = new UserRegistrationRequest("1234","galvis@gmail.com","Aa12345*","Juan Galvis",LocalDate.of(1980,6,25),"2121","Calle 9","Armenia",location,Role.CLIENT);

        var newUser = userService.createUser(user);

        assertNotNull(newUser.id());
        assertEquals(user.email(),newUser.email());
        assertEquals(user.fullName(),newUser.fullName());
        assertEquals(user.birthDate(),newUser.birthDate());
        assertEquals(user.role(),newUser.role());
    }

    @Test
    void testCreateUserThrowsValueConflictExceptionWhenEmailExists() {
        // Sección de Arrange: Se crean los datos del usuario a ser registrado (Con el email de un usuario ya existente).
        var userStore = users.values().stream().findAny().orElseThrow();
        GeoJsonPoint location = new GeoJsonPoint(-75.6801, 4.5350);
        var user = new UserRegistrationRequest("1234",userStore.getEmail(),"Aa12345*","Juan Galvis",LocalDate.of(1980,6,25),"2121","Calle 9","Armenia",location,Role.CLIENT);
        // Sección de Act y Sección de Assert: Ejecute la acción de crear usuario se verifica que genere una excepción debido al email repetido.
        assertThrows(ValueConflictException.class,() -> userService.createUser(user) );
    }

    @Test
    void testGetUserSuccess() {
        // Sección de Arrange: Se obtiene aleatoriamente uno de los usuarios registrado para pruebas.
        var userStore = users.values().stream().findAny().orElseThrow();
        // Sección de Act: Ejecute la acción de obtener usuario basado en su Id.
        var foundUser = userService.getUser(userStore.getId()).orElseThrow();
        // Sección de Assert: Se verifica que los datos obtenidos correspondan a los del usuario almacenado.
        assertEquals(userStore.getName(),foundUser.fullName());
        assertEquals(userStore.getBirthDate(),foundUser.birthDate());
        assertEquals(userStore.getRole(),foundUser.role());
    }

    @Test
    void testGetUserNotFound() {
        // Sección de Arrange: Se crean los datos del usuario a ser registrado (Con el email de un usuario ya existente).
        var id = UUID.randomUUID().toString();
        // Sección de Act: Ejecute la acción de obtener usuario basado en su Id.
        var user = userService.getUser(id);
        // Sección de Assert: Se verifica que los datos obtenidos correspondan a lo esperado.
        assertEquals(Optional.empty(),user);
    }
}
