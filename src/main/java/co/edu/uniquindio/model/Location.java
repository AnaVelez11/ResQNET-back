package co.edu.uniquindio.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "locations")
@Data
public class Location {
    private double longitude;
    private double latitude;
}
