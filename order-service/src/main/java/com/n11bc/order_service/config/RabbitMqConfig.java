package com.n11bc.order_service.config;

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
    public TopicExchange orderExchange(@Value("${app.rabbitmq.order-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange stockExchange(@Value("${app.rabbitmq.stock-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public TopicExchange paymentExchange(@Value("${app.rabbitmq.payment-exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue orderCreatedQueue(@Value("${app.rabbitmq.order-created-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue orderCancelledQueue(@Value("${app.rabbitmq.order-cancelled-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue stockFailedQueue(@Value("${app.rabbitmq.stock-failed-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue paymentSuccessQueue(@Value("${app.rabbitmq.payment-success-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue stockReleasedQueue(@Value("${app.rabbitmq.stock-released-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding orderCreatedBinding(
            Queue orderCreatedQueue,
            TopicExchange orderExchange,
            @Value("${app.rabbitmq.order-created-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Binding orderCancelledBinding(
            Queue orderCancelledQueue,
            TopicExchange orderExchange,
            @Value("${app.rabbitmq.order-cancelled-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderExchange).with(routingKey);
    }

    @Bean
    public Binding stockFailedBinding(
            Queue stockFailedQueue,
            TopicExchange stockExchange,
            @Value("${app.rabbitmq.stock-failed-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(stockFailedQueue).to(stockExchange).with(routingKey);
    }

    @Bean
    public Binding paymentSuccessBinding(
            Queue paymentSuccessQueue,
            TopicExchange paymentExchange,
            @Value("${app.rabbitmq.payment-success-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(paymentSuccessQueue).to(paymentExchange).with(routingKey);
    }

    @Bean
    public Binding stockReleasedBinding(
            Queue stockReleasedQueue,
            TopicExchange stockExchange,
            @Value("${app.rabbitmq.stock-released-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(stockReleasedQueue).to(stockExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
