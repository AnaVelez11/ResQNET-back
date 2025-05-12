package co.edu.uniquindio.controllers.integration;

import co.edu.uniquindio.ProjectApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
@SpringBootTest(classes = ProjectApplication.class)

@AutoConfigureMockMvc
public class CloudinaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadImageToCloudinary() throws Exception {
        // Creamos un archivo simulado
        MockMultipartFile mockFile = new MockMultipartFile(
                "files",                     // Nombre del parámetro esperado en el controlador
                "test-image.jpg",            // Nombre del archivo
                "image/jpeg",                // Tipo de contenido
                "Contenido simulado".getBytes() // Contenido del archivo
        );

        // Enviamos una solicitud POST al endpoint correspondiente (ajusta el endpoint si es diferente)
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/images/upload")
                        .file(mockFile))
                .andExpect(status().isOk()) // Esperamos un 200 OK
                .andExpect(jsonPath("$[0]").exists()); // Esperamos que haya al menos una URL de imagen devuelta
    }
}
*/


