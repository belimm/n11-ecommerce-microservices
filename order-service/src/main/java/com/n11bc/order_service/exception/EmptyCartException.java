package com.n11bc.order_service.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException() {
        super("Cannot create order from an empty cart");
    }
}
