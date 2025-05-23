package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;

import java.util.Optional;

public interface UserService {
    UserResponse createUser(UserRegistrationRequest user);

    Optional<UserResponse> getUser(String id);

    void deactivateUser(String userId);
}
