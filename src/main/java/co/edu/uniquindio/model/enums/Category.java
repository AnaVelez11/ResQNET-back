package co.edu.uniquindio.model.enums;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("categorias")
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    @Indexed(unique = true)
    private String name;
    private String description;
    private CategoryStatus status;
}
