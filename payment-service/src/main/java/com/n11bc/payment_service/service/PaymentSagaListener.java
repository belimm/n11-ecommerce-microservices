package com.n11bc.payment_service.service;

import com.n11bc.payment_service.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSagaListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received StockReservedEvent for order {}", event.orderId());
        paymentService.processStockReserved(event);
    }
}
