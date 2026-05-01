package com.n11bc.cart_service.service;

import com.n11bc.cart_service.event.AbandonedCartEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.cart-exchange}")
    private String cartExchange;

    @Value("${app.rabbitmq.abandoned-cart-routing-key}")
    private String abandonedCartRoutingKey;

    public void publishAbandonedCart(AbandonedCartEvent event) {
        rabbitTemplate.convertAndSend(cartExchange, abandonedCartRoutingKey, event);
        log.info("Published AbandonedCartEvent for cart {}", event.cartId());
    }
}
