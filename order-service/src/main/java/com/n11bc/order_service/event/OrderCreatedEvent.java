package com.n11bc.order_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        BigDecimal totalPrice,
        PaymentCard paymentCard,
        List<OrderCreatedItem> items,
        LocalDateTime createdAt
) {
    public record PaymentCard(
            String cardHolderName,
            String cardNumber,
            String expireMonth,
            String expireYear,
            String cvc
    ) {
    }

    public record OrderCreatedItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
