package com.n11bc.order_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartSnapshotResponse(
        Long id,
        String userId,
        String status,
        List<CartItemSnapshotResponse> items,
        BigDecimal totalPrice,
        LocalDateTime lastActivityAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
