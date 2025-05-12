package co.edu.uniquindio.model;

import co.edu.uniquindio.model.enums.CategoryStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {
    @Id
    private String idCategory;
    private String name;
    private String description;
    private CategoryStatus status;

}
