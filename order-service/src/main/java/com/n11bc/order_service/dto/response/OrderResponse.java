package com.n11bc.order_service.dto.response;

import com.n11bc.order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String userId,
        OrderStatus status,
        BigDecimal totalPrice,
        String paymentMethod,
        String statusReason,
        ShippingAddressResponse shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
