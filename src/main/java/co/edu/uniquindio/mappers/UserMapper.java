package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Mapper para conversión entre entidades User y DTOs relacionados
 * Uso: Convierte entre objetos de negocio (User) y DTOs para la API
 */
@Mapper(componentModel = "spring",
        imports = {UUID.class, ArrayList.class, Role.class, GeoJsonPoint.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    // Convierte UserRegistrationRequest -> User (para creación)
    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "REGISTERED")
    @Mapping(target = "password", expression = "java(co.edu.uniquindio.utils.PasswordEncoderUtil.encode(userDTO.password()))")
    @Mapping(source = "fullName", target = "name")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(target = "role", constant = "CLIENT")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "location", expression = "java(createLocationPoint(userDTO.location().getX(), userDTO.location().getX()))")
    @Mapping(target = "reports", expression = "java(new ArrayList<>())")
    @Mapping(target = "activationCodes", expression = "java(new ArrayList<>())")
    @Mapping(target = "resetCodes", expression = "java(new ArrayList<>())")
    @Mapping(target = "likedReports", expression = "java(new ArrayList<>())")
    @Mapping(target = "deactivationDate", ignore = true)
    User parseOf(UserRegistrationRequest userDTO);

    // Convierte User -> UserResponse (para consultas)
    @Mapping(source = "name", target = "fullName")
    @Mapping(source = "birthDate", target = "birthDate")
    @Mapping(source = "role", target = "role")
    UserResponse toUserResponse(User user);

    /**
     * Método helper para crear un punto de ubicación GeoJSON
     */
    default GeoJsonPoint createLocationPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        return new GeoJsonPoint(longitude, latitude);
    }
}