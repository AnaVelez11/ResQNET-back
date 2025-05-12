// src/test/java/co/edu/uniquindio/services/unit/UserServiceTest.java
package co.edu.uniquindio.services.unit;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.implementations.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequest userRequest;
    private UserResponse userResponse;
    private User user;

    @BeforeEach
    void setUp() {
        GeoJsonPoint location = new GeoJsonPoint(-75.6801, 4.5350);
        user = new User("1234","Juan Galvis","2121","galvis@gmail.com","Aa12345*","Calle 9",LocalDate.of(1980,6,25),"Armenia",Role.CLIENT,UserStatus.ACTIVE,location,true,null,null,null,null,null);
        userRequest = new UserRegistrationRequest(user.getId(), user.getEmail(), user.getPassword(), user.getName(),user.getBirthDate(),user.getPhone(), user.getAddress(), user.getCity(),user.getLocation(),user.getRole());
        userResponse = new UserResponse(user.getId(),user.getEmail(), user.getName(), user.getBirthDate(),user.getPhone(),user.getAddress(),user.getCity(),user.getRole(),user.getStatus(),user.getLocation());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }


    @Test
    void testCreateUserSuccess() {
        Mockito.lenient().when(userMapper.parseOf(userRequest)).thenReturn(user);
        // Arrange: Simular que no existe un usuario con el email dado
        when(userRepository.findByEmail(userRequest.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user); // Simular persistencia
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse); // Simular conversión Entity -> DTO

        // Act: Llamar al método que se está probando
        UserResponse result = userService.createUser(userRequest);

        // Assert: Verificar que los datos devueltos son los esperados
        assertNotNull(result);
        assertEquals(userResponse.id(), result.id());
        assertEquals(userResponse.email(), result.email());

        // Verificar que los mocks fueron llamados correctamente
        verify(userRepository).findByEmail(userRequest.email());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(any(User.class));
    }
}
