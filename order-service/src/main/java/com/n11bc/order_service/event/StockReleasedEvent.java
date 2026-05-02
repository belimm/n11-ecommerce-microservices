package com.n11bc.order_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReleasedEvent(
        Long orderId,
        String userId,
        String reason,
        LocalDateTime releasedAt
) {
}
