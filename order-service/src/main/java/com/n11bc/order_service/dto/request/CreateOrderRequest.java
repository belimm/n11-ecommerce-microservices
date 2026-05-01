package com.n11bc.order_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 80) String addressId,
        @Size(max = 40) String paymentMethod
) {
    public String normalizedPaymentMethod() {
        return paymentMethod == null || paymentMethod.isBlank() ? "IYZICO" : paymentMethod.trim().toUpperCase();
    }
}
