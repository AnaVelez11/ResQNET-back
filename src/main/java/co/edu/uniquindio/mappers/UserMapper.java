package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User parseOf(UserRegistrationRequest request) {
        return User.builder()
                .id(request.id())  // Usa el id si viene, MongoDB lo generará si es null
                .fullName(request.fullName())
                .email(request.email())
                .password(request.password()) // Se encripta en el servicio
                .cellphoneNumber(request.cellphoneNumber())
                .rol(request.rol())
                .status(request.userStatus()) // ⚠ Cambié de request.status() a request.userStatus()
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRol() // ⚠ Eliminé `status` porque no está en `UserResponse`
        );
    }
}


