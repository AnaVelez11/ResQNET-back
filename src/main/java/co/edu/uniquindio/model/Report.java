package co.edu.uniquindio.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Document("reportes")
@Getter
@Setter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public class Report {
        @Id
        @EqualsAndHashCode.Include
        private String id;
        private String title;
        private String description;
        private LocalDateTime date;
        private Location location;
        private List<String> categories;
        private ObjectId idUser;
       // private ReportStatus status;
        // Número de veces que el reporte ha sido marcado como importante
        private int ratingsImportant;
        private List<String> imageUrls;
        private String rejectionReason;
    }
