package com.n11bc.stock_service.service;

import com.n11bc.stock_service.event.StockFailedEvent;
import com.n11bc.stock_service.event.StockReleasedEvent;
import com.n11bc.stock_service.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.stock-exchange}")
    private String stockExchange;

    @Value("${app.rabbitmq.stock-reserved-routing-key}")
    private String stockReservedRoutingKey;

    @Value("${app.rabbitmq.stock-failed-routing-key}")
    private String stockFailedRoutingKey;

    @Value("${app.rabbitmq.stock-released-routing-key}")
    private String stockReleasedRoutingKey;

    public void publishStockReserved(StockReservedEvent event) {
        rabbitTemplate.convertAndSend(stockExchange, stockReservedRoutingKey, event);
        log.info("Published StockReservedEvent for order {}", event.orderId());
    }

    public void publishStockFailed(StockFailedEvent event) {
        rabbitTemplate.convertAndSend(stockExchange, stockFailedRoutingKey, event);
        log.info("Published StockFailedEvent for order {}", event.orderId());
    }

    public void publishStockReleased(StockReleasedEvent event) {
        rabbitTemplate.convertAndSend(stockExchange, stockReleasedRoutingKey, event);
        log.info("Published StockReleasedEvent for order {}", event.orderId());
    }
}
