package co.edu.uniquindio.events;

import co.edu.uniquindio.model.Report;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento personalizado que se dispara cuando:
 * 1. Cambia el estado de un reporte (ej: de PENDING a VERIFIED)
 * 2. Se necesita notificar al usuario dueño del reporte
 * Contiene:
 * - Reporte afectado
 * - ID del usuario dueño
 * - Estado anterior y nuevo del reporte
 */
@Getter
public class ReportStatusChangedEvent extends ApplicationEvent {
    private final Report report;
    private final String userId;       // ID del dueño del reporte
    private final String oldStatus;    // Estado anterior (ej: "PENDING")
    private final String newStatus;    // Estado nuevo (ej: "RESOLVED")

    public ReportStatusChangedEvent(Object source, Report report, String userId, String oldStatus, String newStatus) {
        super(source);
        this.report = report;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}