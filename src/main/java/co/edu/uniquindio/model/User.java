package co.edu.uniquindio.model;

import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import lombok.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class User {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private String phone;
    private String email;
    private String password;
    private String address;
    private LocalDate birthDate;
    private String city;
    private Role role;
    private UserStatus status;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    @Field("active")
    private boolean active = true;

    @Field("deactivationDate")
    private LocalDateTime deactivationDate;

    private List<String> reports;
    private List<ActivationCode> activationCodes;
    private List<ResetCode> resetCodes;

    @Field("likedReports")
    private List<String> likedReports = new ArrayList<>();


}

