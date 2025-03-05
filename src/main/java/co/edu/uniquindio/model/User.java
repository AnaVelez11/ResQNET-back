package co.edu.uniquindio.model;
import co.edu.uniquindio.model.enums.Rol;
import co.edu.uniquindio.model.enums.UserStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class User {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private LocalDate dateBirth;
    private Rol rol;
    private UserStatus status;
}

