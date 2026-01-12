package ro.unitbv.fleet.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ro.unitbv.fleet.config.RabbitConfig;

@Component
public class RouteListener {
    @RabbitListener(queues = RabbitConfig.QUEUE_ROUTE)
    public void recieveRoute(String message) {
        System.out.println("Java has recieved the calculated route: " + message);
    }
}