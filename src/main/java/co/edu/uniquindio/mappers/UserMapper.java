package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    // Convierte UserRegistrationRequest a User
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "REGISTERED")
    @Mapping(target = "password", expression = "java(encryptPassword(userDTO.password()))")
    User parseOf(UserRegistrationRequest userDTO);

    // Convierte User a UserResponse
    UserResponse toUserResponse(User user);

    // Método auxiliar para encriptar la contraseña
    default String encryptPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}
