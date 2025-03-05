package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.exceptions.ValueConflictException;
import co.edu.uniquindio.mappers.UserMapper;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final Map<String, User> userStore = new ConcurrentHashMap<>();
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserRegistrationRequest user) {
        if (userStore.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.email()))) {
            throw new ValueConflictException("Email ya registrado");
        }
        var newUser = userMapper.parseOf(user);
        userStore.put(newUser.getId(), newUser);
        return userMapper.toUserResponse(newUser);
    }
    private String encode(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public Optional<UserResponse> getUser(String id) {
        return Optional.ofNullable(userStore.get(id))
                .map(userMapper::toUserResponse);
    }
}


