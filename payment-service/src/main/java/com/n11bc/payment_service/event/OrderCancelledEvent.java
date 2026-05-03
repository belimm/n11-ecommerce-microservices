package com.n11bc.payment_service.event;

import java.time.LocalDateTime;

public record OrderCancelledEvent(
        Long orderId,
        String orderNumber,
        String userId,
        String reason,
        LocalDateTime cancelledAt
) {
}
