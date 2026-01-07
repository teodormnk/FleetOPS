package ro.unitbv.fleet.service;

import org.springframework.scheduling.annotation.EnableScheduling;
import ro.unitbv.fleet.model.Vehicle;
import ro.unitbv.fleet.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
@EnableScheduling
public class SimulationService {
    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    @Scheduled(fixedRate = 3000)
    public void simulateMovement() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        for(Vehicle v : vehicles) {
            if("BUSY".equals(v.getStatus()) || "AVAILABLE".equals(v.getStatus())) {
                double latChange = (random.nextDouble() - 0.5) * 0.001;
                double lonChange = (random.nextDouble() - 0.5) * 0.001;

                v.setLatitude(v.getLatitude() + latChange);
                v.setLongitude(v.getLongitude() + lonChange);
            }
        }

        vehicleRepository.saveAll(vehicles);
        messagingTemplate.convertAndSend("/topic/fleet", vehicles);
        System.out.println("Simulated movement for vehicles and sent update via WebSocket.");
    }
}
