    package co.edu.uniquindio.model;

    import co.edu.uniquindio.model.enums.ReportStatus;
    import org.bson.types.ObjectId;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
    import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
    import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
    import org.springframework.data.mongodb.core.mapping.Document;
    import lombok.*;
    import java.time.LocalDateTime;
    import java.util.List;

    @Builder
    @Document("reports")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
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
            @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
            private GeoJsonPoint location;
            private ObjectId idUser;
            private String rejectionReason;

            private List<String> categories;
            private List<String> imageUrls;

        }
