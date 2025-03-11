package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;  // 🔹 Reemplazamos el ConcurrentHashMap por MongoDB
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRegistrationRequest request) {
        // Verificar si el ID ya existe en la base de datos
        if (userRepository.existsById(request.id())) {
            throw new ValueConflictException("El ID ya está en uso");
        }

        // Verificar si el email ya está en uso
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ValueConflictException("Email ya registrado");
        }

        // Convertimos el DTO en una entidad User
        User newUser = userMapper.parseOf(request);
        newUser.setPassword(encode(request.password())); // Encriptar la contraseña

        // Guardar en MongoDB
        newUser = userRepository.save(newUser);

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
