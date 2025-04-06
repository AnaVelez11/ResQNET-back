package co.edu.uniquindio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class PasswordResetCode {

    private String code;
    private Date creationDate;
    private Date expirationDate;
    private boolean used;
}
