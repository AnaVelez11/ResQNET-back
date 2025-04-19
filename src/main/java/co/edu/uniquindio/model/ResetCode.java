package co.edu.uniquindio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetCode {

    @Id
    private String id;
    private String code;
    private Date expirationDate;
    private Date creationDate;
    private boolean used;
}
