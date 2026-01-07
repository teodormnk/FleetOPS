package ro.unitbv.fleet.controller;

import ro.unitbv.fleet.model.Order;
import ro.unitbv.fleet.model.Vehicle;
import ro.unitbv.fleet.repository.OrderRepository;
import ro.unitbv.fleet.repository.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FleetController {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Operation(summary = "Shows all vehicles in the fleet in a list")
    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Operation(summary = "Creates a new order")
    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("PENDING");
        return orderRepository.save(order);
    }
}
