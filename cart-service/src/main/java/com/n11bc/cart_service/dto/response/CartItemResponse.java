package com.n11bc.cart_service.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
