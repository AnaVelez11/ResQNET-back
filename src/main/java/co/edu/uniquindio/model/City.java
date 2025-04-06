package co.edu.uniquindio.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cities")
public class City {
    @Id
    private String idCity;
    private String nameCity;

}
