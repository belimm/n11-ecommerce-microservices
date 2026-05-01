package com.n11bc.payment_service.dto.response;

import java.math.BigDecimal;

public record PaymentItemResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
