package co.edu.uniquindio.model;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String phone;
    private String email;
    private String password;
    private String address;
    private Date birthDate;
    private String city;
    private Role role;
    private UserStatus status;

    private List<Report> reports;
    private List<ActivationCode> activationCodes;
    private List<ResetCode> resetCodes;

}

