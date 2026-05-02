package com.n11bc.stock_service.service;

import com.n11bc.stock_service.event.OrderCancelledEvent;
import com.n11bc.stock_service.event.OrderCreatedEvent;
import com.n11bc.stock_service.event.PaymentFailedEvent;
import com.n11bc.stock_service.event.StockFailedEvent;
import com.n11bc.stock_service.event.StockReleasedEvent;
import com.n11bc.stock_service.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockSagaListener {

    private final InventoryService inventoryService;
    private final StockEventPublisher eventPublisher;

    @RabbitListener(queues = "${app.rabbitmq.order-created-queue}")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent received for order {}", event.orderId());
        try {
            StockReservedEvent reservedEvent = inventoryService.reserveStock(event);
            eventPublisher.publishStockReserved(reservedEvent);
        } catch (RuntimeException ex) {
            log.warn("Stock reservation failed for order {}: {}", event.orderId(), ex.getMessage());
            eventPublisher.publishStockFailed(new StockFailedEvent(
                    event.orderId(),
                    event.userId(),
                    ex.getMessage(),
                    LocalDateTime.now()
            ));
        }
    }


    @RabbitListener(queues = "${app.rabbitmq.order-cancelled-queue}")
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent received for order {}", event.orderId());
        StockReleasedEvent releasedEvent = inventoryService.releaseStock(event.orderId(), reasonOrDefault(event.reason()));
        eventPublisher.publishStockReleased(releasedEvent);
    }

    @RabbitListener(queues = "${app.rabbitmq.payment-failed-queue}")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("PaymentFailedEvent received for order {}", event.orderId());
        StockReleasedEvent releasedEvent = inventoryService.releaseStock(event.orderId(), reasonOrDefault(event.reason()));
        eventPublisher.publishStockReleased(releasedEvent);
    }

    private String reasonOrDefault(String reason) {
        return reason == null || reason.isBlank() ? "Payment failed" : reason;
    }
}
