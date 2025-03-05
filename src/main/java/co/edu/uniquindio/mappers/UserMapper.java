package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.utils.PasswordEncoderUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "REGISTERED")
    @Mapping(target = "password", source = "password", qualifiedByName = "encryptPassword")
    User parseOf(UserRegistrationRequest userDTO);

    UserResponse toUserResponse(User user);

    @Named("encryptPassword")
    static String encryptPassword(String password) {
        return PasswordEncoderUtil.encode(password);
    }
}


