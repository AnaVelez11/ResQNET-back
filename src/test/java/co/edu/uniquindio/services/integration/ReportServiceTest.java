package co.edu.uniquindio.services.integration;

import co.edu.uniquindio.data.TestDataLoader;
import co.edu.uniquindio.dto.LocationDTO;
import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.exceptions.ForbiddenActionException;
import co.edu.uniquindio.model.Category;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.CategoryStatus;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.model.enums.UserStatus;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReportServiceTest {

    @Autowired private ReportService reportService;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MongoTemplate mongoTemplate;

    private Map<String, User> users;
    private String userId, otherUserId;
    private String cat1, cat2;

    @BeforeEach
    void setUp() {
        // 1) cargar dos usuarios de prueba
        users = TestDataLoader.loadTestData(userRepository, mongoTemplate);
        var it = users.values().iterator();
        User u1 = it.next(), u2 = it.next();
        userId = u1.getId();
        otherUserId = u2.getId();

        // 2) crear dos categorías activas
        categoryRepository.deleteAll();
        Category c1 = categoryRepository.save(
                Category.builder()
                        .idCategory(UUID.randomUUID().toString())
                        .name("C1")
                        .description("Desc1")
                        .status(CategoryStatus.ACTIVE)
                        .build()
        );
        Category c2 = categoryRepository.save(
                Category.builder()
                        .idCategory(UUID.randomUUID().toString())
                        .name("C2")
                        .description("Desc2")
                        .status(CategoryStatus.ACTIVE)
                        .build()
        );
        cat1 = c1.getIdCategory();
        cat2 = c2.getIdCategory();
    }

    @Test
    void createReport_success() {
        var req = new ReportRequest(
                "Título de prueba",
                "Descripción",
                List.of(cat1),
                new LocationDTO(-75.68, 4.53),null
        );

        ReportResponse resp = reportService.createReport(req, userId);

        assertNotNull(resp.id());
        assertEquals("Título de prueba", resp.title());
        assertEquals(userId, resp.userId());
        assertEquals(1, resp.categories().size());
        assertEquals(cat1, resp.categories().get(0));
        // La ubicación viene ya como GeoJsonPoint por el mapper
        assertTrue(resp.location() instanceof GeoJsonPoint);
    }

    @Test
    void createReport_categoryNotFound_throws() {
        var req = new ReportRequest(
                "X", "Y",
                List.of("no-existe"),
                new LocationDTO(-75.68,4.53),
                null
        );

        assertThrows(ResourceNotFoundException.class,
                () -> reportService.createReport(req, userId));
    }

    @Test
    void updateReport_success() {
        // primero creo
        var create = new ReportRequest("A","B",List.of(cat1),new LocationDTO(-75.68,4.53), null);
        String id = reportService.createReport(create, userId).id();

        // ahora actualizo
        var update = new ReportRequest("NUEVO","Desc nueva",List.of(cat2),new LocationDTO(-75.68,4.53), null);
        ReportResponse updated = reportService.updateReport(update, id, userId);

        assertEquals("NUEVO", updated.title());
        assertEquals(1, updated.categories().size());
        assertEquals(cat2, updated.categories().get(0));
    }

    @Test
    void updateReport_notOwner_throws() {
        // creo con userId
        var create = new ReportRequest("T","D",List.of(cat1),new LocationDTO(-75.68,4.53), null);
        String id = reportService.createReport(create, userId).id();

        // intento update con otro user
        var upd = new ReportRequest("X","Y",List.of(cat1),new LocationDTO(-75.68,4.53), null);
        assertThrows(ForbiddenActionException.class,
                () -> reportService.updateReport(upd, id, otherUserId));
    }

    @Test
    void deleteReport_success_and_forbidden() {
        // creo
        var create = new ReportRequest("DEL","Desc",List.of(cat1),new LocationDTO(-75.68,4.53),null);
        String id = reportService.createReport(create, userId).id();

        // borro OK
        assertDoesNotThrow(() -> reportService.deleteReport(id, userId));

        // creo otro
        var create2 = new ReportRequest("DEL2","Desc2",List.of(cat1),new LocationDTO(-75.68,4.53), null);
        String id2 = reportService.createReport(create2, userId).id();

        // borro por no propietario → Forbidden
        assertThrows(ForbiddenActionException.class,
                () -> reportService.deleteReport(id2, otherUserId));
    }

    @Test
    void toggleImportance_addAndRemove() {
        var create = new ReportRequest("LIKE","Test",List.of(cat2),new LocationDTO(-75.68,4.53), null);
        String id = reportService.createReport(create, userId).id();

        // 1ª llamada → agrega
        assertDoesNotThrow(() -> reportService.toggleReportImportance(id, userId));
        // 2ª llamada → quita
        assertDoesNotThrow(() -> reportService.toggleReportImportance(id, userId));
    }
}
