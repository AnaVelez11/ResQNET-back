package co.edu.uniquindio.model;

import co.edu.uniquindio.model.enums.ReportStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
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
    private String idUser;
    private String rejectionReason;

    // Campos para estado REJECTED
    private LocalDateTime rejectionDate; // Nueva fecha de rechazo
    private LocalDateTime resubmissionDeadline; // Fecha límite para reenvío
    @Builder.Default
    private int resubmissionCount = 0; // Contador de reenvíos

    // Campos para estado VERIFIED
    private String verifiedBy;
    private LocalDateTime verificationDate;

    // Campos para estado RESOLVED
    private String resolvedBy;
    private LocalDateTime resolutionDate;

    @Field("anonymous")
    @Builder.Default
    private boolean anonymous = false;
    private List<String> categories;
    private List<String> imageUrls;

    @Field("likedBy")
    @Builder.Default
    private List<String> likedBy = new ArrayList<>();


}
