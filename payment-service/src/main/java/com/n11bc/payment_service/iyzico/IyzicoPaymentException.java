package com.n11bc.payment_service.iyzico;

public class IyzicoPaymentException extends RuntimeException {

    public IyzicoPaymentException(String message) {
        super(message);
    }

    public IyzicoPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
