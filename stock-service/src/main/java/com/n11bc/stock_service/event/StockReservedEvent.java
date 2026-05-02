package com.n11bc.stock_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StockReservedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        BigDecimal totalPrice,
        PaymentCard paymentCard,
        List<StockReservedItem> items,
        LocalDateTime reservedAt
) {
    public record PaymentCard(
            String cardHolderName,
            String cardNumber,
            String expireMonth,
            String expireYear,
            String cvc
    ) {
    }

    public record StockReservedItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
