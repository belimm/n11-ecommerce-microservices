package com.n11bc.stock_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        BigDecimal totalPrice,
        PaymentCard paymentCard,
        List<OrderCreatedItem> items,
        LocalDateTime createdAt
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentCard(
            String cardHolderName,
            String cardNumber,
            String expireMonth,
            String expireYear,
            String cvc
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderCreatedItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
