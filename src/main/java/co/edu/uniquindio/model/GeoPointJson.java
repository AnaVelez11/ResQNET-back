package co.edu.uniquindio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoPointJson {
    private double latitude;
    private double longitude;
}
