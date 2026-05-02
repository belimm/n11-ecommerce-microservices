package com.n11bc.order_service.service;

import com.n11bc.order_service.event.OrderCancelledEvent;
import com.n11bc.order_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.order-exchange}")
    private String orderExchange;

    @Value("${app.rabbitmq.order-created-routing-key}")
    private String orderCreatedRoutingKey;

    @Value("${app.rabbitmq.order-cancelled-routing-key}")
    private String orderCancelledRoutingKey;

    public void publishOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(orderExchange, orderCreatedRoutingKey, event);
        log.info("Published OrderCreatedEvent for order {}", event.orderId());
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        rabbitTemplate.convertAndSend(orderExchange, orderCancelledRoutingKey, event);
        log.info("Published OrderCancelledEvent for order {}", event.orderId());
    }
}
