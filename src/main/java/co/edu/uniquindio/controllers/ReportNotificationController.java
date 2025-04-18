package co.edu.uniquindio.controllers;

import co.edu.uniquindio.events.NewReportEvent;
import co.edu.uniquindio.dto.NearbyReportNotification;
import co.edu.uniquindio.events.ReportStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ReportNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    /// / Notificar a usuarios cercanos sobre nuevos reportes en su área
    ///
    /// / Envía notificación con detalles del reporte y distancia al usuario afectado
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

    /// / Notificar cambio de estado de un reporte al usuario dueño
    ///
    /// / Envía mensaje de texto con el estado anterior y nuevo
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
                event.getUserId(),          // ID del usuario
                "/queue/status-updates",    // Cola privada para el usuario
                mensaje
        );
    }
}