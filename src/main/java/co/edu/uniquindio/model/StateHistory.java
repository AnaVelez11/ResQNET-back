package co.edu.uniquindio.model;

import co.edu.uniquindio.model.enums.ReportStatus;
import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "state_histories")
public class StateHistory {
    @Id
    private String historyId;
    private Date date;
    private String reason;
    private ReportStatus previousStatus;
    private ReportStatus newStatus;


}
