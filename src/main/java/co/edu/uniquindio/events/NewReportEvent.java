package co.edu.uniquindio.events;

import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

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