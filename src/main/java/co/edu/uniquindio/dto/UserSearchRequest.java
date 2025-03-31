package co.edu.uniquindio.dto;

import java.time.LocalDate;
import java.util.Objects;

public record UserSearchRequest(String fullName,
                                String email,
                                LocalDate dateBirth,
                                Integer page,
                                Integer size) {
    public UserSearchRequest {
        fullName = verifyValue(fullName);
        email = verifyValue(email);
        page = Objects.requireNonNullElse(page,0);
        size = Objects.requireNonNullElse(size,10);
    }

    private String verifyValue(String value){
        return value == null || value.isBlank() ? ".*" : value;
    }
}
