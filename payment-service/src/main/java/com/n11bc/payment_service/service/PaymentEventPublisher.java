package com.n11bc.payment_service.service;

import com.n11bc.payment_service.event.PaymentFailedEvent;
import com.n11bc.payment_service.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.payment-exchange}")
    private String paymentExchange;

    @Value("${app.rabbitmq.payment-success-routing-key}")
    private String paymentSuccessRoutingKey;

    @Value("${app.rabbitmq.payment-failed-routing-key}")
    private String paymentFailedRoutingKey;

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        rabbitTemplate.convertAndSend(paymentExchange, paymentSuccessRoutingKey, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(paymentExchange, paymentFailedRoutingKey, event);
    }
}
