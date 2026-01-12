package ro.unitbv.fleet.config;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_ORDER = "order.queue";
    public static final String QUEUE_ROUTE = "order.route";

    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE_ORDER, true);
    }

    @Bean
    public Queue routeQueue() {
        return new Queue(QUEUE_ROUTE, true);
    }
}