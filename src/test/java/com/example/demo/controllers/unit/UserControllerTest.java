package com.example.demo.controllers.unit;

/*
import co.edu.uniquindio.controllers.UserController;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.services.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserRegistrationRequest user;
    private UserResponse userResponse;
    private UserService userService;

    @BeforeEach
    void setup() {
        user = new UserRegistrationRequest("1234","juan@example.com","passworD1333", "Juan Pérez", LocalDate.of(1980,6,25),"321",
                "calle 2","Rionegro");
        userResponse = new UserResponse(UUID.randomUUID().toString(),user.email(), user.fullName(), user.dateBirth(),user.phone(),user.address(),user.city(),Role.CLIENT, UserStatus.ACTIVE);
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        // Sección de Arrange: Se configura la respuesta simulada por el componente userService,
        // en este cado se indica que cuando se envíe la solicitud de creación debe retornar la respuesta dada.
        Mockito.when(userService.createUser(Mockito.any(UserRegistrationRequest.class))).thenReturn(userResponse);
        // Sección de Act: Ejecute la acción de invocación del servicio de registro de usuarios
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                // Sección de Assert: Se verifica que los datos obtenidos correspondan a los del usuario registrado.
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.fullName").value(user.fullName()))
                .andExpect(jsonPath("$.dateBirth").value(user.dateBirth().toString()))
                .andExpect(jsonPath("$.phone").value(user.phone()))
                .andExpect(jsonPath("$.address").value(user.address()))
                .andExpect(jsonPath("$.city").value(user.city()))
                .andExpect(jsonPath("$.role").value(userResponse.role().toString()))
                .andExpect(jsonPath("$.status").value(userResponse.status().toString()));;
    }

    @Test
    void testCreateUserValueConflictExceptionWhenEmailExists() throws Exception {
        // Sección de Arrange: Se configura la respuesta simulada por el componente userService,
        // en este cado se indica que cuando se envíe la solicitud de creación debe generar una excepción de tipo ValueConflictException.
        Mockito.when(userService.createUser(Mockito.any(UserRegistrationRequest.class))).thenThrow(ValueConflictException.class);

        // Sección de Act: Ejecute la acción de invocación del servicio de registro de usuarios
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                // Sección de Assert: Se verifica que el resultado obtenido corresponda a lo esperado un status code de conflicto.
                .andExpect(status().isConflict());
    }

    @Test
    void testGetUserSuccess() throws Exception {
        // Sección de Arrange: Se configura la respuesta simulada por el componente userService,
        // en este cado se indica que cuando se envíe la solicitud de creación debe retornar la respuesta dada.
        Mockito.when(userService.getUser(userResponse.id())).thenReturn(Optional.of(userResponse));
        // Sección de Act: Ejecute la acción de invocación del servicio de consulta de usuarios
        mockMvc.perform(get("/users/"+userResponse.id()))
                // Sección de Assert: Se verifica que los datos obtenidos correspondan a los del usuario esperado.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.fullName").value(user.fullName()))
                .andExpect(jsonPath("$.dateBirth").value(user.dateBirth().toString()))
                .andExpect(jsonPath("$.phone").value(user.phone()))
                .andExpect(jsonPath("$.address").value(user.address()))
                .andExpect(jsonPath("$.city").value(user.city()))
                .andExpect(jsonPath("$.role").value(userResponse.role().toString()))
                .andExpect(jsonPath("$.status").value(userResponse.status().toString()));
    }

    @Test
    void testGetUserNotFound() throws Exception {
        // Sección de Arrange: Se configura la respuesta simulada por el componente userService,
        // en este cado se indica que cuando se envíe la solicitud de creación debe retornar la respuesta dada.
        Mockito.when(userService.getUser(userResponse.id())).thenReturn(Optional.empty());
        // Sección de Act: Ejecute la acción de invocación del servicio de consulta de usuarios
        mockMvc.perform(get("/users/"+userResponse.id()))
                // Sección de Assert: Se verifica que la respuesta obtenida sea la esperada (404).
                .andExpect(status().isNotFound());
    }
}

 */