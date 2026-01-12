package ro.unitbv.fleet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import ro.unitbv.fleet.config.RabbitConfig;

import ro.unitbv.fleet.model.Order;
import ro.unitbv.fleet.model.Vehicle;
import ro.unitbv.fleet.repository.OrderRepository;
import ro.unitbv.fleet.repository.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FleetController {
    private static final Logger logger = LoggerFactory.getLogger(FleetController.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final Counter routeCalculationCounter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public FleetController(VehicleRepository vehicleRepository,
                           OrderRepository orderRepository,
                           MeterRegistry meterRegistry) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.routeCalculationCounter = Counter.builder("fleet.routes.calculated")
                .description("Total number of route calculations performed")
                .register(meterRegistry);
    }

    @Operation(summary = "Shows all vehicles in the fleet in a list")
    @GetMapping("/vehicles")
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Operation(summary = "Creates a new order (ASync via RabbitMQ)")
    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("PENDING");
        Order saved = orderRepository.save(order);

        try{
            String orderJson = new ObjectMapper().findAndRegisterModules().writeValueAsString(saved);
            rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_ORDER, orderJson);
            logger.info("Order sent to RabbitMQ: " + saved.getId());
        } catch (Exception e) {
            logger.error("Failed to send order to RabbitMQ", e);
        }

        return saved;
    }
}
