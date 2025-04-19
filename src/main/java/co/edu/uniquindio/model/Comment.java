package co.edu.uniquindio.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")

public class Comment {
    @Id
    private String idComment;
    private Date date;
    private String content;
    private String idReport;
    private String idUser;

}
