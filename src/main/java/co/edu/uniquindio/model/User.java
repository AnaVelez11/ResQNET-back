package co.edu.uniquindio.model;
import co.edu.uniquindio.model.enums.Rol;
import co.edu.uniquindio.model.enums.UserStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String fullName;
    private String email;
    private String password;
    private String cellphoneNumber;
    private Rol rol;
    private UserStatus status;
}

