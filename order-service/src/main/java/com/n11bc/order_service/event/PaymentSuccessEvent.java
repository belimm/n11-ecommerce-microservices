package com.n11bc.order_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSuccessEvent(
        Long orderId,
        String orderNumber,
        String userId,
        String paymentId,
        String conversationId,
        BigDecimal paidPrice,
        String currency,
        LocalDateTime paidAt
) {
}
