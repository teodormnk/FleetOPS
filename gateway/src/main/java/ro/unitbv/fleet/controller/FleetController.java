package ro.unitbv.fleet.controller;

import ro.unitbv.fleet.dto.RouteResponse;
import ro.unitbv.fleet.model.Order;
import ro.unitbv.fleet.model.Vehicle;
import ro.unitbv.fleet.repository.OrderRepository;
import ro.unitbv.fleet.repository.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.HttpHeaders; 
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FleetController {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final String ROUTING_SERVICE_URL = "http://routing-service:8081/route";

    @Operation(summary = "Shows all vehicles in the fleet in a list")
    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Operation(summary = "Creates a new order")
    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("PENDING");
        
        try{
            RestTemplate restTemplate = new RestTemplate();
            String requestJson = """
                    {
                       "startLat": 44.4268, "startLon": 26.1025,
                        "endLat": 44.5548, "endLon": 26.0846
                    }
                    """;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            ResponseEntity<RouteResponse> response = restTemplate.postForEntity(
                    ROUTING_SERVICE_URL,
                    request,
                    RouteResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                RouteResponse route = response.getBody();
                System.out.println("Ruta calculata (km): " + route.getDistanceKm());
            } 
        } 
        catch (Exception e) {
            System.err.println("Eroare la apelarea serviciului de rutare: " + e.getMessage());
        }

        return orderRepository.save(order);
    }
}
