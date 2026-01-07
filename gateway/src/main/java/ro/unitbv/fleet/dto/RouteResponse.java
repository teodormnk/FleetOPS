package ro.unitbv.fleet.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RouteResponse {
    private Double distanceKm;
    private Integer estimatedDurationSeconds;
    private List<Map<String, Double>> points;
}
