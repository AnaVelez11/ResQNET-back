package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRegistrationRequest request) {
        // Verificar si el email ya está en uso
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ValueConflictException("Email ya registrado");
        }

        // Construcción del usuario con los atributos correctos
        User newUser = User.builder()
                .id(request.id())
                .name(request.fullName())
                .email(request.email())
                .password(encode(request.password())) //Encriptar contraseña
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .birthDate(java.sql.Date.valueOf(request.dateBirth())) //Convertir LocalDate a Date
                .role(Role.CLIENT)
                .status(UserStatus.REGISTERED)
                .activationCodes(new ArrayList<>()) // Lista vacía de códigos de activación
                .reports(new ArrayList<>()) // Lista vacía de reportes
                .build();

        // Guardar el usuario en MongoDB
        newUser = userRepository.save(newUser);

        // Retornar la respuesta mapeada
        return userMapper.toUserResponse(newUser);
    }

    private String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Optional<UserResponse> getUser(String id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse);
    }

}
