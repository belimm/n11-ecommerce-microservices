package com.n11bc.stock_service.event;

import java.time.LocalDateTime;

public record StockFailedEvent(
        Long orderId,
        String userId,
        String reason,
        LocalDateTime failedAt
) {
}
