package com.n11bc.payment_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReservedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        BigDecimal totalPrice,
        PaymentCard paymentCard,
        List<StockReservedItem> items,
        LocalDateTime reservedAt
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
    public record StockReservedItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
