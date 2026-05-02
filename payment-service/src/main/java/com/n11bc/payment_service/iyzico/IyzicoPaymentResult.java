package com.n11bc.payment_service.iyzico;

import java.math.BigDecimal;

public record IyzicoPaymentResult(
        boolean successful,
        String status,
        String paymentId,
        String conversationId,
        BigDecimal price,
        BigDecimal paidPrice,
        String currency,
        String errorMessage
) {
    public static IyzicoPaymentResult failed(String message) {
        return new IyzicoPaymentResult(false, "failure", null, null, null, null, null, message);
    }
}
