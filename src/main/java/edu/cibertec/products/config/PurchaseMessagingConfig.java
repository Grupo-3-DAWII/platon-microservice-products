package edu.cibertec.products.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.Map;

@Configuration
public class PurchaseMessagingConfig {

    @Bean
    public Declarables purchaseValidationTopology(
            @Value("${app.rabbitmq.purchase.exchange:platon.purchase.events}") String exchangeName,
            @Value("${app.rabbitmq.purchase.validation-queue:platon.products.purchase.validation}") String queueName,
            @Value("${app.rabbitmq.purchase.request-routing-key:purchase.requested}") String routingKey) {
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        Queue requests = new Queue(queueName, true, false, false, Map.of("x-message-ttl", 30000));
        Queue rejected = new Queue("platon.products.payment.rejected", true);
        return new Declarables(exchange, requests, rejected,
                BindingBuilder.bind(requests).to(exchange).with(routingKey),
                BindingBuilder.bind(rejected).to(exchange).with("payment.rejected"));
    }

    @Bean
    public SimpleRabbitListenerContainerFactory stockReturnListenerFactory(ConnectionFactory connectionFactory) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(true);
        factory.setPrefetchCount(10);
        return factory;
    }
}
