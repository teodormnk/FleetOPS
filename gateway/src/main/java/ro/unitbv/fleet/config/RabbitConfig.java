package ro.unitbv.fleet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;

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