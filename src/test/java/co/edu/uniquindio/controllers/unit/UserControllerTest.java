package co.edu.uniquindio.controllers.unit;

import co.edu.uniquindio.config.JwtTokenFilter;
import co.edu.uniquindio.config.SecurityConfig;
import co.edu.uniquindio.controllers.UserController;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.services.interfaces.UserService;
import co.edu.uniquindio.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Optional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(controllers = UserController.class)
@Import({JwtTokenFilter.class, SecurityConfig.class})
@WithMockUser(roles = "ADMIN")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;
    private UserRegistrationRequest user;
    private UserResponse userResponse;
    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        user = new UserRegistrationRequest("1234","galvis@gmail.com","Aa12345*","Juan Galvis",LocalDate.of(1980,6,25),"2121","Calle 9","Armenia",null,Role.CLIENT);
        userResponse = new UserResponse(
                user.id(),
                user.email(),
                user.fullName(),
                user.birthDate(),
                user.phone(),
                user.address(),
                user.city(),
                Role.CLIENT,
                UserStatus.ACTIVE,
                new GeoJsonPoint(-75.6801, 4.5350)
        );
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        Mockito.when(userService.createUser(Mockito.any(UserRegistrationRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                // Sección de Assert: Se verifica que los datos obtenidos correspondan a los del usuario registrado.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value(user.fullName()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.role").value(user.role().toString()));
    }

    @Test
    void testCreateUserValueConflictExceptionWhenEmailExists() throws Exception {
        Mockito.when(userService.createUser(Mockito.any(UserRegistrationRequest.class))).thenThrow(ValueConflictException.class);

        mockMvc.perform(post("/api/users")                        // <- ruta corregida
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetUserSuccess() throws Exception {

        Mockito.when(userService.getUser(userResponse.id())).thenReturn(Optional.of(userResponse));

        mockMvc.perform(get("/api/users/" + userResponse.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.fullName").value(user.fullName()))
                .andExpect(jsonPath("$.birthDate").value(user.birthDate().toString()))
                .andExpect(jsonPath("$.phone").value(user.phone()))
                .andExpect(jsonPath("$.address").value(user.address()))
                .andExpect(jsonPath("$.city").value(user.city()))
                .andExpect(jsonPath("$.role").value(userResponse.role().toString()))
                .andExpect(jsonPath("$.status").value(userResponse.status().toString()));
    }

    @Test
    void testGetUserNotFound() throws Exception {

        Mockito.when(userService.getUser(userResponse.id())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/" + userResponse.id()))   // <- ruta corregida
                .andExpect(status().isNotFound());
    }

}
