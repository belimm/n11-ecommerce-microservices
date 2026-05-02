package com.n11bc.payment_service.dto.response;

import com.n11bc.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        Long id,
        Long orderId,
        String orderNumber,
        String userId,
        String conversationId,
        String iyzicoPaymentId,
        PaymentStatus status,
        BigDecimal price,
        BigDecimal paidPrice,
        String currency,
        String iyzicoStatus,
        String failureReason,
        List<PaymentItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
