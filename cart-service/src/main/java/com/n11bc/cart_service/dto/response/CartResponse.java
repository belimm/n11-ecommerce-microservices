package com.n11bc.cart_service.dto.response;

import com.n11bc.cart_service.entity.CartStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Long id,
        String userId,
        CartStatus status,
        List<CartItemResponse> items,
        BigDecimal totalPrice,
        LocalDateTime lastActivityAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
