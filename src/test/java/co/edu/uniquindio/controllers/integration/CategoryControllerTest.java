package co.edu.uniquindio.controllers.integration;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.dto.CategoryRequest;
import co.edu.uniquindio.dto.CategoryResponse;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(authorities = "ADMIN")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Limpiar y precargar una categoría base
        categoryRepository.deleteAll();
        categoryRepository.save(Category.builder()
                .idCategory("base")
                .name("Base")
                .description("Categoría base")
                .status(CategoryStatus.ACTIVE)
                .build());
    }

    @Test
    void testCreateCategorySuccess() throws Exception {
        var req = new CategoryRequest("Nueva", "Desc nueva");

        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Nueva"))
                .andExpect(jsonPath("$.description").value("Desc nueva"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testCreateCategoryConflict() throws Exception {
        // "Base" ya existe
        var req = new CategoryRequest("Base", "Cualquier");
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetAllActive() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Base"));
    }

    @Test
    void testGetAllByStatus() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("status","ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/categories")
                        .param("status","DELETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetCategoryByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/categories/base"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Base"));
    }

    @Test
    void testGetCategoryByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/categories/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCategorySuccess() throws Exception {
        var req = new CategoryRequest("Renomb", "Desc mod");
        mockMvc.perform(put("/api/categories/base")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renomb"))
                .andExpect(jsonPath("$.description").value("Desc mod"));
    }

    @Test
    void testDeleteCategorySuccess() throws Exception {
        mockMvc.perform(delete("/api/categories/base"))
                .andExpect(status().isNoContent());
        // Ahora debería estar en estado DELETED
        mockMvc.perform(get("/api/categories")
                        .param("status","DELETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testDeleteCategoryNotFound() throws Exception {
        mockMvc.perform(delete("/api/categories/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCategoryNames() throws Exception {
        mockMvc.perform(get("/api/categories/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Base"));
    }
}
