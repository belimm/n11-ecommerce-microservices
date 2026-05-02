package com.n11bc.payment_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
