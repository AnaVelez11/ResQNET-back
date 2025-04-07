package co.edu.uniquindio.model;

import co.edu.uniquindio.model.enums.ReportStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Document("reports")
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public class Report {
        @Id
        @EqualsAndHashCode.Include
        private String id;
        private String title;
        private String description;
        private LocalDateTime date;
        private int ratingsImportant; // Número de veces que el reporte ha sido marcado como importante
        private ReportStatus status;
        private Location location;

        private List<String> categories;
        private List<String> imageUrls;

    }
