package com.n11bc.payment_service.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Long orderId) {
        super("Payment not found for order id: " + orderId);
    }
}
