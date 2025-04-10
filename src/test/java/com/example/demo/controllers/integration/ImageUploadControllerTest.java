package com.example.demo.controllers.integration;

import co.edu.uniquindio.ProjectApplication;
import co.edu.uniquindio.services.interfaces.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
 /*
@SpringBootTest(classes = ProjectApplication.class)
@AutoConfigureMockMvc
public class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CloudinaryService cloudinaryService() {
            return Mockito.mock(CloudinaryService.class);
        }
    }

    @Autowired
    private CloudinaryService cloudinaryService;

    @Test
    void testUploadImagesSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
        );
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file))
                .andExpect(status().isOk());
    }
}


  */