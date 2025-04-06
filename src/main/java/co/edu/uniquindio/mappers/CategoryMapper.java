package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.dto.UserResponse;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "ACTIVE")
    UserResponse toUserResponse(User user);
    Category parseOf(CategoryRequest categoryRequest);
    CategoryResponse toCategoryResponse(Category category);
}