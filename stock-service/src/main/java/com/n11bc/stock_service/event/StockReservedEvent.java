package com.n11bc.stock_service.event;

import java.time.LocalDateTime;
import java.util.List;

public record StockReservedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        List<StockReservedItem> items,
        LocalDateTime reservedAt
) {
    public record StockReservedItem(
            Long productId,
            String productName,
            int quantity
    ) {
    }
}
