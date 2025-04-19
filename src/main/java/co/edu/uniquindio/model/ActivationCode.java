package co.edu.uniquindio.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activation_codes")

public class ActivationCode {
    private Date creationDate;
    private String code;
    private Date expirationDate;
    private boolean used;


}
