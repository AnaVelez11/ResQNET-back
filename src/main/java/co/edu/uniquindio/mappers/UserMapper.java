package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.UserRegistrationRequest;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "REGISTERED")
    @Mapping(target = "password" , expression = "java( new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(userDTO.password()) )")
    User parseOf(UserRegistrationRequest userDTO);

    UserResponse toUserResponse(User user);
}


