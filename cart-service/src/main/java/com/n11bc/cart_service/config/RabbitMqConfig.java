package com.n11bc.cart_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange cartExchange(@Value("${app.rabbitmq.cart-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue abandonedCartQueue(@Value("${app.rabbitmq.abandoned-cart-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding abandonedCartBinding(
            Queue abandonedCartQueue,
            TopicExchange cartExchange,
            @Value("${app.rabbitmq.abandoned-cart-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(abandonedCartQueue).to(cartExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
