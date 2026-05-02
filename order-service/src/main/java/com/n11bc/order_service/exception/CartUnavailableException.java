package com.n11bc.order_service.exception;

public class CartUnavailableException extends RuntimeException {

    public CartUnavailableException(String message) {
        super(message);
    }
}
