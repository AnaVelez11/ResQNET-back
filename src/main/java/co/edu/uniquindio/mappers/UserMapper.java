package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * Mapper para conversión entre entidades User y DTOs relacionados
 * Uso: Convierte entre objetos de negocio (User) y DTOs para la API
 */
@Mapper(componentModel = "spring", imports = {UUID.class})
public interface UserMapper {

    // Convierte UserRegistrationRequest -> User (para creación)
    // - Genera ID automático UUID
    // - Establece estado inicial REGISTERED
    // - Encripta la contraseña automáticamente
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "REGISTERED")
    @Mapping(target = "password", expression = "java(co.edu.uniquindio.utils.PasswordEncoderUtil.encode(userDTO.password()))")

    User parseOf(UserRegistrationRequest userDTO);

    // Convierte User -> UserResponse (para consultas)
    // - Mapea todos los campos automáticamente
    UserResponse toUserResponse(User user);
}
