package co.edu.uniquindio.controllers;

import co.edu.uniquindio.events.NewReportEvent;
import co.edu.uniquindio.dto.NearbyReportNotification;
import co.edu.uniquindio.events.ReportStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReportNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleNewReportEvent(NewReportEvent event) {
        NearbyReportNotification notification = NearbyReportNotification.builder()
                .reportId(event.getReport().getId())
                .title(event.getReport().getTitle())
                .distance(event.getDistanceInKm())
                .categories(event.getReport().getCategories())
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        messagingTemplate.convertAndSendToUser(
                event.getUserId(),
                "/queue/nearby-reports",
                notification
        );
    }
    @EventListener
    public void handleReportStatusChanged(ReportStatusChangedEvent event) {
        String mensaje = String.format(
                "Reporte #%s: Cambió de %s a %s",
                event.getReport().getId(),
                event.getOldStatus(),
                event.getNewStatus()
        );

        // Envía SOLO al usuario dueño del reporte (usando su ID)
        messagingTemplate.convertAndSendToUser(
                event.getUserId(),          // ID del usuario (ej: "123")
                "/queue/status-updates",    // Cola privada para el usuario
                mensaje
        );
    }
}