package com.n11bc.cart_service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AbandonedCartEvent(
        Long cartId,
        String userId,
        BigDecimal totalPrice,
        List<AbandonedCartItem> items,
        LocalDateTime abandonedAt
) {
    public record AbandonedCartItem(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
