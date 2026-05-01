package com.n11bc.stock_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentFailedEvent(
        Long orderId,
        String orderNumber,
        String userId,
        String reason,
        LocalDateTime failedAt
) {
}
