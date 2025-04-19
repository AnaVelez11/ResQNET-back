package co.edu.uniquindio.events;

import co.edu.uniquindio.model.Report;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento personalizado que se dispara cuando:
 * 1. Se crea un nuevo reporte en el sistema
 * 2. Se necesita notificar a usuarios cercanos
 * Contiene:
 * - Reporte generado
 * - ID del usuario a notificar
 * - Distancia en km entre el usuario y el reporte
 */
@Getter
public class NewReportEvent extends ApplicationEvent {
    private final Report report;
    private final String userId;
    private final double distanceInKm;

    public NewReportEvent(Object source, Report report, String userId, double distanceInKm) {
        super(source);
        this.report = report;
        this.userId = userId;
        this.distanceInKm = distanceInKm;
    }
}