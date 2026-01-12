package ro.unitbv.fleet.service;

@Component
public class RouteListener {
    @RabbitListener(queues = RabbitConfig.QUEUE_ROUTE)
    public void recieveRoute(String message) {
        System.out.println("Java has recieved the calculated route: " + message);
    }
}