package co.edu.uniquindio.services.unit;

import co.edu.uniquindio.dto.ReportRequest;
import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.dto.LocationDTO;
import co.edu.uniquindio.events.NewReportEvent;
import co.edu.uniquindio.exceptions.ForbiddenActionException;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.mappers.ReportMapper;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.model.enums.ReportStatus;
import co.edu.uniquindio.model.enums.Role;
import co.edu.uniquindio.repositories.CategoryRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.CloudinaryService;
import co.edu.uniquindio.services.implementations.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ReportServiceTest {

    @Mock ReportRepository reportRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock CloudinaryService cloudinaryService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock ReportMapper reportMapper;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock co.edu.uniquindio.services.implementations.EmailServiceImpl emailService;

    @InjectMocks
    ReportServiceImpl service;

    private final String userId = "u1";


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReport_success() {
        // Arrange
        String userId = "u1";
        User user = User.builder()
                .id(userId)
                .reports(null)
                .build();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(categoryRepository.existsById("c1")).willReturn(true);

        ReportRequest req = new ReportRequest(
                "Título", "Desc",
                List.of("c1"),
                new LocationDTO(-75.68, 4.53),
                List.of()
        );

        Report saved = Report.builder()
                .id("r1")
                .title("Título")
                .description("Desc")
                .location(new GeoJsonPoint(-75.68, 4.53))
                .categories(List.of("c1"))
                .idUser(userId)
                .status(ReportStatus.PENDING)
                .date(LocalDateTime.now())
                .ratingsImportant(0)
                .imageUrls(null)
                .build();
        given(reportRepository.save(any(Report.class))).willReturn(saved);

        // Stub para el mapper
        ReportResponse expectedDto = ReportResponse.builder()
                .id("r1")
                .title("Título")
                .description("Desc")
                .location(saved.getLocation())
                .categories(saved.getCategories())
                .status("PENDING")
                .ratingsImportant(0)
                .userId(userId)
                .imageUrls(List.of())
                .message("Reporte creado exitosamente.")
                .build();
        given(reportMapper.toResponse(saved)).willReturn(expectedDto);

        // <<< NUEVO: simulamos usuario cercano >>>
        User otherUser = User.builder()
                .id("u2")
                .location(saved.getLocation())
                .build();
        given(userRepository.findUsersNearLocation(
                eq(saved.getLocation().getX()),
                eq(saved.getLocation().getY()),
                anyDouble(),
                eq(userId)))
                .willReturn(List.of(otherUser));

        // Act
        ReportResponse actual = service.createReport(req, userId);

        // Assert
        assertEquals("r1", actual.id());
        assertEquals("Título", actual.title());
        assertEquals("Reporte creado exitosamente.", actual.message());

        // Verificamos que el usuario original haya sido actualizado
        verify(userRepository).save(argThat(u -> u.getReports().contains("r1")));

        // Verificamos que se haya publicado un evento por cada usuario cercano
        then(eventPublisher).should().publishEvent(argThat(ev -> ev instanceof NewReportEvent));
    }


    @Test
    void createReport_userNotFound_throws() {
        given(userRepository.findById("uX")).willReturn(Optional.empty());

        ReportRequest req = new ReportRequest("T","D",List.of(), new LocationDTO(0,0),null);

        assertThrows(ResourceNotFoundException.class, () ->
                service.createReport(req, "uX")
        );
    }

    @Test
    void createReport_categoryNotFound_throws() {
        String userId = "u1";
        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(categoryRepository.existsById("bad")).willReturn(false);

        ReportRequest req = new ReportRequest(
                "T","D", List.of("bad"),
                new LocationDTO(0,0),null
        );

        assertThrows(ResourceNotFoundException.class, () ->
                service.createReport(req, userId)
        );
    }

    @Test
    void updateReport_success() {
        // prepara un reporte en estado PENDING
        Report r = Report.builder()
                .id("r2").idUser(userId)
                .status(ReportStatus.PENDING)
                .location(new GeoJsonPoint(0,0))
                .categories(List.of("c1"))
                .imageUrls(List.of())
                .build();
        given(reportRepository.findById("r2")).willReturn(Optional.of(r));
        given(categoryRepository.existsById("c2")).willReturn(true);

        ReportRequest req = new ReportRequest("X","Y",List.of("c2"),
                new co.edu.uniquindio.dto.LocationDTO(1,1), List.of());
        Report saved = r.toBuilder().title("X").description("Y")
                .location(new GeoJsonPoint(1,1)).categories(List.of("c2"))
                .date(LocalDateTime.now()).build();
        given(reportRepository.save(any())).willReturn(saved);
        given(reportMapper.toResponse(saved)).willReturn(
                ReportResponse.builder()
                        .id("r2").title("X").description("Y")
                        .location(saved.getLocation()).categories(saved.getCategories())
                        .status("PENDING").ratingsImportant(0)
                        .userId(userId).imageUrls(List.of()).message("ok")
                        .build()
        );

        ReportResponse out = service.updateReport(req,"r2",userId);
        assertEquals("X", out.title());
    }

    @Test
    void updateReport_forbiddenOnWrongStatus() {
        Report r = Report.builder().id("r3").idUser(userId)
                .status(ReportStatus.RESOLVED).build();
        given(reportRepository.findById("r3")).willReturn(Optional.of(r));
        ReportRequest req = new ReportRequest("T","D",List.of(),new co.edu.uniquindio.dto.LocationDTO(0,0), List.of());
        assertThrows(ForbiddenActionException.class,
                () -> service.updateReport(req,"r3",userId));
    }

    @Test
    void deleteReport_success() {
        String reportId = "r2";
        String userId  = "u2";

        // 1) Report en estado PENDING
        Report report = Report.builder()
                .id(reportId)
                .status(ReportStatus.PENDING)
                .idUser(userId)
                .build();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // 2) User con rol CLIENT y listas no nulas
        User user = new User();
        user.setId(userId);
        user.setRole(Role.CLIENT);
        user.setReports(new ArrayList<>(List.of(reportId)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 3) invoca
        service.deleteReport(reportId, userId);

        // 4) verifica interacciones
        verify(reportRepository).deleteById(reportId);
        assertFalse(user.getReports().contains(reportId));
        verify(userRepository).save(user);
    }


    @Test
    void toggleReportImportance_addAndRemove() {
        Report r = Report.builder().id("r5").ratingsImportant(0).build();
        User u = new User(); u.setId(userId); u.setLikedReports(new ArrayList<>());
        given(reportRepository.findById("r5")).willReturn(Optional.of(r));
        given(userRepository.findById(userId)).willReturn(Optional.of(u));

        // 1) marca como importante
        service.toggleReportImportance("r5", userId);
        assertEquals(1, r.getRatingsImportant());
        assertTrue(u.getLikedReports().contains("r5"));

        // 2) quita importancia
        service.toggleReportImportance("r5", userId);
        assertEquals(0, r.getRatingsImportant());
        assertFalse(u.getLikedReports().contains("r5"));
    }

    @Test
    void calculateDistance_basic() {
        double d = service.calculateDistance(0, 0, 0, 1);
        assertTrue(d > 100 && d < 200);  // ~111km per grado
    }

}
