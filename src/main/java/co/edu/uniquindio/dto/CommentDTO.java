package co.edu.uniquindio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres")
    private String content;

    @NotBlank(message = "Debe especificarse el ID del reporte")
    private String idReport;
}
