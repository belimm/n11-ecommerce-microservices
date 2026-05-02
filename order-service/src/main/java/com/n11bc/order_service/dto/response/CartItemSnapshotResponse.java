package com.n11bc.order_service.dto.response;

import java.math.BigDecimal;

public record CartItemSnapshotResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
