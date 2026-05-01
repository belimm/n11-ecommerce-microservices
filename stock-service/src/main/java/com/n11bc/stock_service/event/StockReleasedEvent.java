package com.n11bc.stock_service.event;

import java.time.LocalDateTime;

public record StockReleasedEvent(
        Long orderId,
        String userId,
        String reason,
        LocalDateTime releasedAt
) {
}
