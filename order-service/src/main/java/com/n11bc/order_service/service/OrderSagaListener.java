package com.n11bc.order_service.service;

import com.n11bc.order_service.entity.OrderStatus;
import com.n11bc.order_service.event.StockFailedEvent;
import com.n11bc.order_service.event.StockReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${app.rabbitmq.stock-failed-queue}")
    public void onStockFailed(StockFailedEvent event) {
        log.info("StockFailedEvent received for order {}", event.orderId());
        orderService.applySagaStatus(event.orderId(), OrderStatus.CANCELLED, reasonOrDefault(event.reason(), "Stock reservation failed"));
    }

    @RabbitListener(queues = "${app.rabbitmq.stock-released-queue}")
    public void onStockReleased(StockReleasedEvent event) {
        log.info("StockReleasedEvent received for order {}", event.orderId());
        orderService.applySagaStatus(event.orderId(), OrderStatus.CANCELLED, reasonOrDefault(event.reason(), "Stock released after payment failure"));
    }

    private String reasonOrDefault(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason;
    }
}
